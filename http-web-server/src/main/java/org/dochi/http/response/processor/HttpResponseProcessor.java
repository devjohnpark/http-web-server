package org.dochi.http.response.processor;

//import org.dochi.buffer.Http11ResponseHandler;
import org.dochi.http.api.HttpApiResponse;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpResponseProcessor extends HttpApiResponse {
    void recycle();
    void flush() throws IOException;
}
