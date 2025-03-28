package com.kv.webflux.logging.provider;

import com.kv.webflux.logging.client.LoggingProperties;
import com.kv.webflux.logging.client.LoggingUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

public final class CookieProvider {


    public String createClientRequestMessage(MultiValueMap<String, String> cookies, LoggingProperties properties) {
        if (!properties.isLogCookies()) {
            return LoggingUtils.EMPTY_MESSAGE;
        }

        return properties.getMaskedCookies() == null
                ? extractInClientRequest(cookies)
                : extractInClientRequest(setMaskInClientRequest(cookies, properties.getMaskedCookies()));
    }

    public String createServerRequestMessage(MultiValueMap<String, HttpCookie> cookies, LoggingProperties properties) {
        if (!properties.isLogCookies()) {
            return LoggingUtils.EMPTY_MESSAGE;
        }

        return properties.getMaskedCookies() == null
                ? extractInServerRequest(cookies)
                : extractInServerRequest(setMaskInServerRequest(cookies, properties.getMaskedCookies()));
    }

    public String createResponseMessage(MultiValueMap<String, ResponseCookie> cookies, LoggingProperties properties) {
        if (!properties.isLogCookies()) {
            return LoggingUtils.EMPTY_MESSAGE;
        }

        return properties.getMaskedCookies() == null
                ? extractInResponse(cookies)
                : extractInResponse(setMaskInResponse(
                cookies,
                properties.getMaskedCookies(),
                properties.getVisibleCharsInMaskedValue()
        ));
    }


    private Map<String, List<String>> setMaskInClientRequest(Map<String, List<String>> cookies,
                                                             String[] cookiesToMask) {
        return ProviderUtils.setMaskToValues(cookies, cookiesToMask, LoggingUtils.DEFAULT_MASK);
    }

    private Map<String, List<HttpCookie>> setMaskInServerRequest(Map<String, List<HttpCookie>> cookies,
                                                                 String[] cookiesToMask) {

        LinkedCaseInsensitiveMap<List<HttpCookie>> cookiesToLog = toCaseInsensitive(cookies);
        for (String name : cookiesToMask) {
            ProviderUtils.setMaskToValue(cookiesToLog, name, new HttpCookie(name, LoggingUtils.DEFAULT_MASK));
        }

        return cookiesToLog;
    }

    private Map<String, List<ResponseCookie>> setMaskInResponse(
            MultiValueMap<String, ResponseCookie> cookies,
            String[] cookiesToMask,
            Integer visibleChars
    ) {
        LinkedCaseInsensitiveMap<List<ResponseCookie>> cookiesToLog = toCaseInsensitive(cookies);

        for (String cookieToMask : cookiesToMask) {
            List<ResponseCookie> cookieToLog = cookiesToLog.get(cookieToMask);
            if (cookieToLog == null || cookieToLog.isEmpty()) {
                continue;
            }

            List<ResponseCookie> maskedCookies = cookieToLog.stream()
                    .map(cookie -> maskResponseCookie(
                                    cookieToMask,
                                    cookie,
                                    visibleChars
                            )
                    )
                    .toList();

            cookiesToLog.put(cookieToMask, maskedCookies);
        }

        return cookiesToLog;
    }

    private ResponseCookie maskResponseCookie(
            String cookieToMask,
            ResponseCookie cookie,
            Integer visibleChars
    ) {
        if (cookie.getValue().contains("null")) {
            return cookie;
        }
        if (visibleChars == null) {
            return ResponseCookie.from(cookieToMask, LoggingUtils.DEFAULT_MASK).build();
        }

        String maskedValue = "{value-to-mask-shorter-than-" + visibleChars + "}";
        if (cookie.getValue().length() > visibleChars) {
            maskedValue = cookie.getValue().substring(0, visibleChars) + "...";
        }
        return ResponseCookie.from(cookieToMask, maskedValue).build();
    }

    private String extractInClientRequest(Map<String, List<String>> cookies) {
        StringBuilder sb = new StringBuilder(" COOKIES: [ ");

        cookies.forEach((name, values) -> values
                .forEach(value -> sb.append(name).append("=").append(value).append(" ")));

        return sb.append("]").toString();
    }

    private String extractInServerRequest(Map<String, List<HttpCookie>> cookies) {
        StringBuilder sb = new StringBuilder(" COOKIES: [ ");

        cookies.forEach((name, values) -> values
                .forEach(httpCookie -> sb.append(httpCookie).append(" ")));

        return sb.append("]").toString();
    }

    private String extractInResponse(Map<String, List<ResponseCookie>> cookies) {
        StringBuilder sb = new StringBuilder(" COOKIES (Set-Cookie): [ ");

        cookies.forEach((name, values) -> values
                .forEach(responseCookie -> sb.append(" [").append(responseCookie).append("]").append(" ")));

        return sb.append("]").toString();
    }

    private <T> LinkedCaseInsensitiveMap<List<T>> toCaseInsensitive(Map<String, List<T>> cookies) {
        LinkedCaseInsensitiveMap<List<T>> map = new LinkedCaseInsensitiveMap<>();
        map.putAll(cookies);
        return map;
    }
}
