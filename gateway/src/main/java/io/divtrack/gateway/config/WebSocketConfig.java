package io.divtrack.gateway.config;

import io.divtrack.gateway.websocket.PriceFanOutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

/**
 * Registers PriceFanOutHandler at the /ws path.
 *
 * CRITICAL — Order=-1 explained:
 * ───────────────────────────────
 * Spring Cloud Gateway intercepts ALL requests and tries to match routes
 * defined in application.yml. Without order=-1, when a browser tries to
 * upgrade to ws://gateway/ws, Spring Cloud Gateway would intercept it and
 * try to proxy it to a backend service — NOT what we want.
 *
 * Setting order=-1 means this HandlerMapping runs BEFORE Spring Cloud
 * Gateway's RoutePredicateHandlerMapping (which has order=1 by default).
 * So /ws is handled by our fan-out handler first, and SCG never sees it.
 *
 * WHAT SPRING CLOUD GATEWAY CAN DO vs WHAT WE MUST DO:
 * ──────────────────────────────────────────────────────
 * SCG CAN:  proxy WebSocket connections transparently to ONE backend
 *           (e.g., route /ws to ws://market-data-service:8083/ws)
 *           — everything passes through like a tunnel
 *
 * SCG CANNOT: act as a WebSocket server AND fan-out to N connected clients
 *             based on Redis pub/sub messages. That's custom code.
 *             THIS is why we write PriceFanOutHandler ourselves.
 *
 * If we used SCG to proxy WS, we'd have:
 *   Browser WS → Gateway (passes through) → Market Data Service WS
 * But then Market Data manages sessions — which means when we scale to
 * 3 Market Data pods, we're back to sticky sessions. Avoid this.
 *
 * Our approach:
 *   Browser WS → Gateway WS Server (PriceFanOutHandler manages sessions)
 *   Redis prices:live → Gateway (fan-out to sessions)
 *   = Redis decouples the price publisher from the session manager
 */
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final PriceFanOutHandler priceFanOutHandler;

    /**
     * Map /ws to our fan-out handler.
     * Order -1 = higher priority than Spring Cloud Gateway routes.
     */
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of("/ws", priceFanOutHandler));
        mapping.setOrder(-1);
        return mapping;
    }

    /**
     * Required adapter bean — converts WebSocket upgrade requests
     * to reactive WebSocketHandler invocations.
     * Without this bean, the handler mapping is registered but nothing
     * actually processes the WS upgrade.
     */
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
