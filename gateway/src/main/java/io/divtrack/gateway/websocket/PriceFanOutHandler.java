package io.divtrack.gateway.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * The core fan-out component.
 *
 * Two responsibilities:
 * 1. WebSocketHandler — accepts browser WS connections, parses ticker subscriptions
 * 2. Fan-out target — called by RedisSubscriberConfig when a price update arrives
 *
 * HOW THE CONNECTION FLOW WORKS:
 * ─────────────────────────────
 * 1. Browser opens: ws://gateway:3000/ws?tickers=STRC,ARCC
 * 2. handle() is called with the new WebSocketSession
 * 3. We parse "STRC,ARCC" from the query string
 * 4. We register the session for each ticker in SessionRegistry
 * 5. We subscribe to incoming messages (for SUBSCRIBE/UNSUBSCRIBE commands)
 * 6. When the connection closes, we deregister from all tickers
 *
 * HOW FAN-OUT WORKS:
 * ─────────────────
 * When Market Data publishes to Redis prices:live:
 *   RedisSubscriberConfig.receive() → parses ticker from JSON → calls fanOut(ticker, json)
 *   fanOut() → gets all sessions for that ticker → sends to each one
 *
 * REACTIVE CHALLENGE:
 * ───────────────────
 * This runs on Reactor threads. Never block — use flatMap not map+block.
 * session.send() returns Mono<Void>, so we compose it with Flux.flatMap.
 * Errors per-session are caught and used to remove stale sessions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceFanOutHandler implements WebSocketHandler {

    private final SessionRegistry registry;
    private final ObjectMapper objectMapper;

    // ── 1. Accept browser WebSocket connections ─────────────────────────────

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Parse ?tickers=STRC,ARCC from the handshake URI
        List<String> tickers = parseTickersFromQuery(session);
        tickers.forEach(t -> registry.register(t, session));

        log.info("[WS] New connection id={} subscribed to tickers={} total={}",
                session.getId(), tickers, registry.totalConnections());

        return session.receive()
                // Handle SUBSCRIBE / UNSUBSCRIBE messages sent from the browser
                .doOnNext(msg -> handleClientMessage(session, msg.getPayloadAsText()))
                .doOnError(e -> log.warn("[WS] Error on session={}: {}", session.getId(), e.getMessage()))
                // Clean up when browser disconnects (clean close or crash)
                .doFinally(signal -> {
                    registry.deregister(session);
                    log.info("[WS] Session disconnected id={} signal={} remaining={}",
                            session.getId(), signal, registry.totalConnections());
                })
                .then();
    }

    // ── 2. Fan-out: called by Redis subscriber when price update arrives ─────

    /**
     * Push a price update to ALL sessions watching this ticker.
     *
     * This is the "one Redis message → N browser pushes" step.
     *
     * @param ticker  e.g. "STRC"
     * @param jsonMsg e.g. {"type":"PRICE_UPDATE","ticker":"STRC","yield":11.5,"price":99.8}
     */
    public Mono<Void> fanOut(String ticker, String jsonMsg) {
        var sessions = registry.getSessionsFor(ticker);
        if (sessions.isEmpty()) {
            return Mono.empty(); // nobody watching this ticker right now
        }

        log.debug("[FanOut] ticker={} → {} sessions", ticker, sessions.size());

        return Flux.fromIterable(sessions)
                .flatMap(session ->
                    session.send(Mono.just(session.textMessage(jsonMsg)))
                           .onErrorResume(e -> {
                               // Send failed = stale session (client crashed without clean close)
                               log.debug("[FanOut] Removing stale session={}", session.getId());
                               registry.deregister(session);
                               return Mono.empty(); // continue with remaining sessions
                           })
                )
                .then();
    }

    // ── 3. Handle client-sent SUBSCRIBE / UNSUBSCRIBE messages ──────────────

    /**
     * Browser can send dynamic subscription changes after connection:
     * {"type":"SUBSCRIBE",   "tickers":["STRC","ARCC"]}
     * {"type":"UNSUBSCRIBE", "tickers":["STRC"]}
     */
    private void handleClientMessage(WebSocketSession session, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.path("type").asText();
            switch (type) {
                case "SUBSCRIBE" ->
                    node.path("tickers").forEach(t -> registry.register(t.asText(), session));
                case "UNSUBSCRIBE" ->
                    // Simple: deregister from all, then re-register remaining
                    registry.deregister(session);
                default ->
                    log.debug("[WS] Unknown message type={} from session={}", type, session.getId());
            }
        } catch (Exception e) {
            log.warn("[WS] Unparseable message from session={}: {}", session.getId(), payload);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Extract tickers from ws://host/ws?tickers=STRC,ARCC,JEPI */
    private List<String> parseTickersFromQuery(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery(); // "tickers=STRC,ARCC"
        if (query == null || !query.contains("tickers=")) {
            return List.of();
        }
        String raw = Arrays.stream(query.split("&"))
                .filter(p -> p.startsWith("tickers="))
                .findFirst()
                .map(p -> p.replace("tickers=", ""))
                .orElse("");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
