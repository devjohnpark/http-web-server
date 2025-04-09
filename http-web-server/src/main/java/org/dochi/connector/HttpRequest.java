package org.dochi.connector;

import org.dochi.internal.InternalInputStream;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;
import java.io.InputStream;

public interface HttpRequest {
    Part getPart(String partName) throws IOException, HttpStatusException;
    String getMethod();
    String getRequestURI();
    String getPath();
    String getQueryString();
    String getProtocol();
    String getHeader(String key);
    String getCookie();
    String getContentType();
    int getContentLength();
    String getParameter(String key) throws IOException;
    String getCharacterEncoding();
    InternalInputStream getInputStream() throws IOException;
}

