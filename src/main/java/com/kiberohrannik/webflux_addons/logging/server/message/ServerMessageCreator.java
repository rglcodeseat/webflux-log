package com.kiberohrannik.webflux_addons.logging.server.message;

import com.kiberohrannik.webflux_addons.logging.server.RequestData;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ServerMessageCreator {

//    Mono<String> createForRequest(ServerWebExchange exchange);
    Mono<RequestData> createForRequest(ServerWebExchange exchange);

    String createForResponse(ServerWebExchange exchange);
}