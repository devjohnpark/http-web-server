package org.dochi.http.processor;

import org.dochi.webserver.RequestMapper;
import org.dochi.webserver.SocketWrapper;

public interface HttpProcessor {
    void process(SocketWrapper socketWrapper, RequestMapper requestMapper);
}
