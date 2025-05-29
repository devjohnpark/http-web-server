package org.dochi.connector;

import org.dochi.internal.Request;
import org.dochi.external.HttpExternalRequest;

import java.io.IOException;

public interface RequestHandler extends HttpExternalRequest {
    void recycle() throws IOException;
    Request getRequest();
}

