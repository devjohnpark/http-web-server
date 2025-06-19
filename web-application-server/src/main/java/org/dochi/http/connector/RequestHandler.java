package org.dochi.http.connector;

import org.dochi.http.data.raw.Request;
import org.dochi.http.external.HttpExternalRequest;
import org.dochi.http.internal.buffer.InputBuffer;

public interface RequestHandler extends HttpExternalRequest {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
    Request getRequest();
}

