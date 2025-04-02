package org.dochi.buffer.connector;

import org.dochi.buffer.internal.InternalInputStream;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.HttpMethod;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;
import java.io.InputStream;

public interface HttpRequest {
    Part getPart(String partName) throws IOException, HttpStatusException;
    InputStream getInputStream() throws IOException;
    String getMethod();
    String getRequestURI();
    String getPath();
    String getQueryString();
    HttpVersion getHttpVersion();
    String getHeader(String key);
    String getCookie();
    String getContentType();
    int getContentLength();
    String getConnection();
    String getRequestParameter(String key);
    InternalInputStream getDochiInputStream() throws IOException;
}

