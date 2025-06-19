package org.dochi.http.connector;

import org.dochi.http.external.HttpExternalResponse;

import java.io.IOException;

public interface ResponseHandler extends HttpExternalResponse {
    void setOutputStream(TmpBufferedOutputStream outputStream);
    void recycle();
    void flush() throws IOException;
}
