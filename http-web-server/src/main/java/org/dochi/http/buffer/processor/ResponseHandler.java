package org.dochi.http.buffer.processor;

//import org.dochi.buffer.Http11ResponseHandler;
import org.dochi.inputbuffer.external.HttpExternalResponse;
import org.dochi.inputbuffer.socket.SocketWrapperBase;

import java.io.IOException;

public interface ResponseHandler extends HttpExternalResponse {
    void init(SocketWrapperBase<?> socketWrapper);
    void recycle();
    void flush() throws IOException;
}
