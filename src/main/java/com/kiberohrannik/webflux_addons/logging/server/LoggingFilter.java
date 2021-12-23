package com.kiberohrannik.webflux_addons.logging.server;

import com.kiberohrannik.webflux_addons.logging.server.message.logger.ServerRequestLogger;
import com.kiberohrannik.webflux_addons.logging.server.message.logger.ServerResponseLogger;
import com.kiberohrannik.webflux_addons.logging.server.message.logger.TimeElapsedLogger;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class LoggingFilter implements WebFilter {

    private final ServerRequestLogger requestMessageCreator;
    private final ServerResponseLogger responseMessageCreator;
    private final TimeElapsedLogger timeElapsedLogger;


    LoggingFilter(ServerRequestLogger requestMessageCreator,
                  ServerResponseLogger responseMessageCreator,
                  TimeElapsedLogger timeElapsedLogger) {

        this.requestMessageCreator = requestMessageCreator;
        this.responseMessageCreator = responseMessageCreator;
        this.timeElapsedLogger = timeElapsedLogger;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startMillis = System.currentTimeMillis();

        ServerHttpRequest loggedRequest = requestMessageCreator.log(exchange);
        ServerHttpResponse loggedResponse = responseMessageCreator.log(exchange, startMillis);

        return chain.filter(exchange.mutate().request(loggedRequest).response(loggedResponse).build())
                .doFinally(signalType -> timeElapsedLogger.log(startMillis, exchange.getLogPrefix()));
    }
}