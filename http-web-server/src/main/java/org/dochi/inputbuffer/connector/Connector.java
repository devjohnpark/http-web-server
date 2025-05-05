package org.dochi.inputbuffer.connector;

import org.dochi.http.request.data.HttpVersion;
import org.dochi.webserver.lifecycle.LifecycleBase;

public class Connector extends LifecycleBase {
    private final HttpVersion httpVersion;

    public Connector(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Connector() {
        this.httpVersion = HttpVersion.HTTP_1_1;
    }

    public Request createRequest() {
        return new Request(this);
    }
}
