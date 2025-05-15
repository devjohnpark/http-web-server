package org.dochi.http.buffer.processor;

//import org.dochi.buffer.Http11ResponseProcessor;
import org.dochi.http.api.HttpApiResponse;
import org.dochi.inputbuffer.socket.SocketWrapperBase;

import java.io.IOException;

public interface HttpResponseProcessor extends HttpApiResponse {
    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
    void recycle();
    void flush() throws IOException;
}
