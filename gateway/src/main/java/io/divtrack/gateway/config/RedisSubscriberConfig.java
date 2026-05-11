package io.divtrack.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.divtrack.gateway.websocket.PriceFanOutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;

/**
 * Subscribes the Gateway to Redis channel: prices:live
 *
 * HOW REDIS PUB/SUB WORKS (vs normal Redis GET/SET):
 * ────────────────────────────────────────────────────
 * Normal Redis:  client asks  → Redis responds   (request/response)
 * Pub/Sub Redis: subscriber listens → publisher fires → Redis pushes to ALL subscribers
 *               (push model — subscriber never polls)
 *
 * THE BRIDGE:
 * Market Data Service  →  PUBLISH prices:live "{ticker:'STRC', yield:11.5}"
 *                         ↓ Redis delivers to ALL subscribers
 * Gateway (this bean)  ←  RECEIVE message
 *                         ↓
 *                      PriceFanOutHandler.fanOut("STRC", json)
 *                         ↓
 *                      All WS sessions watching STRC receive the update
 *
 * CHALLENGE — Redis reconnect:
 * If Redis restarts, the subscription is lost. We use .retry() to
 * automatically re-subscribe when the connection comes back.
 * ReactiveRedisMessageListenerContainer also handles reconnect internally.
 *
 * CHALLENGE — Multiple Gateway pods:
 * Each pod subscribes to prices:live independently.
 * ALL pods receive EVERY message. Each pod fans out only to ITS OWN sessions.
 * This is the key insight: Redis pub/sub + in-memory session registry
 * solves the "sticky session" problem without any coordination between pods.
 */
@Slf4j
@Configuration
public class RedisSubscriberConfig {

    @Bean
    public ReactiveRedisMessageListenerContainer priceListenerContainer(
            ReactiveRedisConnectionFactory factory,
            PriceFanOutHandler fanOutHandler,
            ObjectMapper objectMapper
    ) {
        var container = new ReactiveRedisMessageListenerContainer(factory);

        container
                .receive(ChannelTopic.of("prices:live"))        // subscribe to channel
                .map(message -> message.getMessage())           // extract JSON string
                .flatMap(json -> {
                    try {
                        JsonNode node   = objectMapper.readTree(json);
                        String  ticker  = node.path("ticker").asText();

                        if (ticker.isBlank()) {
                            log.warn("[Redis] Received price update with no ticker: {}", json);
                            return reactor.core.publisher.Mono.empty();
                        }

                        return fanOutHandler.fanOut(ticker, json);  // ← THE FAN-OUT
                    } catch (Exception e) {
                        log.error("[Redis] Failed to parse price update: {}", json, e);
                        return reactor.core.publisher.Mono.empty();
                    }
                })
                .doOnError(e -> log.error("[Redis] Subscription error on prices:live", e))
                .retry()                                         // re-subscribe if Redis drops
                .subscribe();

        log.info("[Redis] Subscribed to channel: prices:live");
        return container;
    }
}
