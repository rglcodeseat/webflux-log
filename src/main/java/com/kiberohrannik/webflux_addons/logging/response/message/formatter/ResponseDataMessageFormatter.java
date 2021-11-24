package com.kiberohrannik.webflux_addons.logging.response.message.formatter;

import com.kiberohrannik.webflux_addons.logging.LoggingProperties;
import com.kiberohrannik.webflux_addons.logging.response.message.ResponseData;
import reactor.core.publisher.Mono;

public interface ResponseDataMessageFormatter {

    Mono<ResponseData> addData(LoggingProperties loggingProperties, Mono<ResponseData> sourceMessage);
}