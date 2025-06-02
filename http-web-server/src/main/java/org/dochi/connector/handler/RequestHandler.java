package org.dochi.connector.handler;

import org.dochi.internal.Request;
import org.dochi.external.HttpExternalRequest;
import org.dochi.internal.buffer.InputBuffer;

import java.io.IOException;

public interface RequestHandler extends HttpExternalRequest {
    void setInputBuffer(InputBuffer inputBuffer);
    void recycle();
    Request getRequest();
}

