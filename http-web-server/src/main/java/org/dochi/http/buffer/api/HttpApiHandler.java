package org.dochi.http.buffer.api;

import org.dochi.http.api.HttpApiResponse;
import org.dochi.inputbuffer.external.HttpExternalRequest;
import org.dochi.inputbuffer.external.HttpExternalResponse;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(HttpExternalRequest request, HttpExternalResponse response) throws IOException;
    void destroy();
}
