package com.kiberohrannik.webflux_addons.logging.response.message.formatter;

import com.kiberohrannik.webflux_addons.logging.LoggingProperties;
import com.kiberohrannik.webflux_addons.logging.response.message.ResponseData;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public class BodyMessageFormatter implements ResponseDataMessageFormatter {

    @Override
    public Mono<ResponseData> addData(LoggingProperties loggingProperties,
                                      Mono<ResponseData> sourceMessage) {

        if (loggingProperties.isLogBody()) {
            return sourceMessage.flatMap(source -> addBody(source.getResponse(), source.getLogMessage()));
        }

        return sourceMessage;
    }

    
    private Mono<ResponseData> addBody(ClientResponse response, String source) {
        return response.bodyToMono(String.class)
                .map(body -> {
                    ClientResponse cloned = response.mutate().body(body).build();
                    return new ResponseData(cloned, formatMessage(body, source));
                })
                .switchIfEmpty(Mono.just(new ResponseData(response, formatMessage("", source))));
    }


    private String formatMessage(String body, String source) {
        return source.concat("\nBODY: [ ").concat(body).concat(" ]");
    }
}