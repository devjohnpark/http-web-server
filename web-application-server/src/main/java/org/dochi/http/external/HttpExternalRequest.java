package org.dochi.http.external;

import org.dochi.http.exception.HttpStatusException;

import java.io.IOException;
import java.io.InputStream;

public interface HttpExternalRequest {
    Part getPart(String partName) throws IOException, HttpStatusException;
    String getMethod();
    String getRequestURI();
    String getPath();
    String getQueryString();
    String getProtocol();
    String getHeader(String key);
    String getContentType();
    int getContentLength();
    String getParameter(String key) throws IOException;
    String getCharacterEncoding();
    InputStream getInputStream() throws IOException;
}

