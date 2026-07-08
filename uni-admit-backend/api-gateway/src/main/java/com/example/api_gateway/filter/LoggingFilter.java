package com.example.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        // Inject correlation ID for tracing
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Correlation-ID", correlationId)
                .build();

        log.info("[{}] → {} {}", correlationId,
                request.getMethod(), request.getPath());

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value() : 0;
                    log.info("[{}] ← {} {} {}ms",
                            correlationId, statusCode,
                            request.getPath(), duration);
                });
    }

    @Override
    public int getOrder() {
        return -2; // Run before JwtAuthFilter
    }
}
