package org.dochi.http.api;

import org.dochi.http.external.HttpExternalRequest;
import org.dochi.http.external.HttpExternalResponse;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(HttpExternalRequest request, HttpExternalResponse response) throws IOException;
    void destroy();
}
