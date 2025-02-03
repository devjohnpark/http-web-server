package org.dochi.http.api;

import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebServiceConfig config);
    void service(HttpApiRequest request, Http11ResponseProcessor response) throws IOException;
    void destroy();
}
