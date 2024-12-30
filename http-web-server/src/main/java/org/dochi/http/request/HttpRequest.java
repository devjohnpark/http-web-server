package org.dochi.http.request;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.dochi.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private final BufferedReader br;
    private RequestLine requestLine;
    private final RequestHeaders headers = new RequestHeaders();
    private final RequestParameters parameters = new RequestParameters();

    public HttpRequest(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public boolean prepareRequest() throws IOException, IllegalArgumentException {
        if ((requestLine = getRequestLine()) == null) {
            return false;
        }
        setHeaders();
        setParameters();
        return true;
    }

    public void refresh() {
        headers.clearHeaders();
        parameters.clearParameters();
    }

    private RequestLine getRequestLine() throws IOException, IllegalArgumentException {
        String line = br.readLine();
        if (line == null) {
            return null;
        }
        return RequestLine.createFromRequestLine(line);
    }

    private void setHeaders() throws IOException {
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            headers.addHeader(line);
        }
    }

    private void setParameters() throws IOException {
        parameters.addRequestParameters(requestLine.getQueryString());
        if (ResourceType.URL.getMimeType().equals(headers.getContentType())) {
            parameters.addRequestParameters(getBodyAsString());
        }
    }

    // body에 대한 정확한 content-length 받아야, HTTP 트랜잭션이 끝나는 시점에 기존 메시지의 끝과 새로운 메세지의 시작점을 정확히 알수있다.
    public String getBodyAsString() throws IOException {
        int contentLength = headers.getContentLength();
        char[] body = new char[contentLength];
        int actualLength = br.read(body, 0, contentLength);
        return String.copyValueOf(body, 0, actualLength);
    }

    public byte[] getBodyAsBytes() throws IOException {
        return getBodyAsString().getBytes(StandardCharsets.UTF_8);
    }

    public BufferedReader getBufferedReader() { return br; }

    public HttpMethod getMethod() { return requestLine.getMethod(); }

    public String getPath() { return requestLine.getPath(); }

    public String getRequestParameter(String key) { return parameters.getParameter(key); }

    public HttpVersion getHttpVersion() { return requestLine.getVersion(); }

    public String getHeader(String key) { return headers.getHeader(key); }

    public String getCookie() { return headers.getCookie(); }

    public String getContentType() { return headers.getContentType(); }

    public int getContentLength() { return headers.getContentLength(); }

    public String getConnection() { return headers.getConnection(); }
}
