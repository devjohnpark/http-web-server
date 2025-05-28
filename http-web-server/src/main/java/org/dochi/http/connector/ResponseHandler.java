package org.dochi.http.connector;

import org.dochi.external.HttpExternalResponse;
import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;

public interface ResponseHandler extends HttpExternalResponse {
    void init(SocketWrapperBase<?> socketWrapper);
    void recycle();
    void flush() throws IOException;
}
