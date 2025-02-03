package org.dochi.http.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.processor.HttpRequestProcessor;
import org.dochi.http.response.HttpResponseProcessor;
import org.dochi.webserver.socket.SocketWrapper;

public interface HttpProcessor {
    void process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper);
}
