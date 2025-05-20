package org.dochi.http.handler;

import org.dochi.internal.Request;
import org.dochi.external.HttpExternalRequest;

import java.io.IOException;

// HttpProcessor에서 HttpRequestProcessor을 제어하므로 해당 객체에서만 호출 필요
public interface RequestHandler extends HttpExternalRequest {
//    boolean isPrepareHeader() throws IOException;
//    boolean isProcessHeader() throws IOException;
//    protected void setInputBuffer(InputBuffer inputBuffer);
//    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
//    boolean isProcessHeader() throws IOException;
    void recycle() throws IOException;
    Request getRequest();
}

