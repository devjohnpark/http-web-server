package org.dochi.http.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.socket.SocketWrapper;

public interface HttpProcessor {
    void process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper);
}
