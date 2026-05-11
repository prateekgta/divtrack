package io.divtrack.gateway.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * In-memory registry: ticker → set of live WebSocket sessions.
 *
 * WHY ConcurrentHashMap + CopyOnWriteArraySet?
 * ─────────────────────────────────────────────
 * Multiple Reactor threads can register/deregister sessions concurrently.
 * ConcurrentHashMap makes per-ticker map writes thread-safe.
 * CopyOnWriteArraySet makes individual session set iteration thread-safe
 * (reads never block even while a write is in progress).
 *
 * CHALLENGE — Stale sessions:
 * A browser closing the tab without a clean WS close frame leaves a stale
 * session in this map. We don't detect it here — PriceFanOutHandler catches
 * the send() error and calls deregister() at that point. This is lazy cleanup.
 *
 * At MVP scale (hundreds of users) this is fine.
 * At 10k+ connections, consider a heartbeat ping/pong and scheduled cleanup.
 */
@Slf4j
@Component
public class SessionRegistry {

    // "STRC" → {sessionA, sessionB, sessionC}
    // "JEPI" → {sessionD}
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> tickerSessions =
            new ConcurrentHashMap<>();

    /**
     * Register a session for a specific ticker.
     * Called when browser sends SUBSCRIBE or on initial WS handshake.
     */
    public void register(String ticker, WebSocketSession session) {
        tickerSessions
                .computeIfAbsent(ticker.toUpperCase(), k -> new CopyOnWriteArraySet<>())
                .add(session);
        log.debug("[Registry] session={} subscribed ticker={} (total sessions for ticker: {})",
                session.getId(), ticker.toUpperCase(),
                tickerSessions.get(ticker.toUpperCase()).size());
    }

    /**
     * Remove a session from ALL tickers it was watching.
     * Called on WS connection close or on send error (stale session cleanup).
     */
    public void deregister(WebSocketSession session) {
        tickerSessions.values().forEach(sessions -> sessions.remove(session));
        log.debug("[Registry] session={} deregistered from all tickers", session.getId());
    }

    /**
     * Get all live sessions watching a ticker.
     * Returns an empty set if no one is watching — never null.
     */
    public Set<WebSocketSession> getSessionsFor(String ticker) {
        return tickerSessions.getOrDefault(ticker.toUpperCase(), Collections.emptySet());
    }

    /** Diagnostic — total connected sessions across all tickers. */
    public int totalConnections() {
        return tickerSessions.values().stream().mapToInt(Set::size).sum();
    }
}
