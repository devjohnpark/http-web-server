package org.dochi.buffer;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
    SocketState process(SocketWrapperBase<?> socketWrapper);
}
