package org.dochi.webserver.config;

import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.attribute.HttpResAttribute;

public class HttpAttribute implements HttpConfig {
    private final HttpReqAttribute httpReqConfig;
    private final HttpResAttribute httpResConfig;

    public HttpAttribute(HttpReqAttribute httpReqConfig, HttpResAttribute httpResConfig) {
        this.httpReqConfig = httpReqConfig;
        this.httpResConfig = httpResConfig;
    }

    public HttpReqConfig getHttpReqConfig() {
        return httpReqConfig;
    }

    public HttpResConfig getHttpResConfig() {
        return httpResConfig;
    }
}
