package org.dochi.connector;

import org.dochi.external.HttpExternalResponse;
import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;

public interface ResponseHandler extends HttpExternalResponse {
    void setOutputStream(BufferedOutputStream outputStream);
    void recycle();
    void flush() throws IOException;
}
