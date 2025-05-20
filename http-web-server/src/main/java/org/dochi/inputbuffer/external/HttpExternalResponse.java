package org.dochi.inputbuffer.external;

//import org.dochi.buffer.Http11ResponseHandler;
import org.dochi.http.api.HttpApiResponse;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.response.ResponseHeaders;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpExternalResponse {
    HttpExternalResponse addHeader(String key, String value);

    HttpExternalResponse addCookie(String cookie);

    HttpExternalResponse addVersion(HttpVersion version);

    HttpExternalResponse addConnection(boolean isKeepAlive);

    HttpExternalResponse addKeepAlive(int timeout, int maxRequests);

    HttpExternalResponse addStatus(HttpStatus status);

    HttpExternalResponse addDateHeaders(String date);

    HttpExternalResponse addContentHeaders(String contentType, int contentLength);

    HttpExternalResponse inActiveDateHeader();

    HttpExternalResponse activeDateHeader();

    void sendNoContent() throws IOException;

    void send(HttpStatus status) throws IOException;

    void send(HttpStatus status, byte[] body, String contentType) throws IOException;

    void sendError(HttpStatus status) throws IOException;

    void sendError(HttpStatus status, String errorMessage) throws IOException;

    OutputStream getOutputStream();
}
