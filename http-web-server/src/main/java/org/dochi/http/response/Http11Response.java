package org.dochi.http.response;

import org.dochi.webserver.config.HttpResConfig;

public class Http11Response implements HttpResponse {
    private final Http11ResponseStream responseStream;

    public Http11Response(Http11ResponseStream responseStream, HttpResConfig config) {
        this.responseStream = responseStream;
    }


}
