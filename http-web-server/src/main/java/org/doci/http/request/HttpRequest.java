package org.doci.http.request;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.doci.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private BufferedReader br;
    private RequestLine requestLine;
    private final RequestHeaders headers = new RequestHeaders();
    private final RequestParameters parameters = new RequestParameters();

    public HttpRequest(InputStream in) {
        try {
            this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            processHttpRequest();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHttpRequest() throws IOException {
        requestLine = getRequestLine();
        setHeaders();
        setParameters();
    }

    private RequestLine getRequestLine() throws IOException {
        String line = br.readLine();
        if (line == null) {
            throw new IllegalStateException();
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
            parameters.addRequestParameters(getAllBodyAsString());
        }
    }

    public String getAllBodyAsString() throws IOException {
        int contentLength = headers.getContentLength();
        char[] body = new char[contentLength];
        int actualLength = br.read(body, 0, contentLength);
        return String.copyValueOf(body, 0, actualLength);
    }

    public byte[] getBodyAsBytes() throws IOException {
        return getAllBodyAsString().getBytes(StandardCharsets.UTF_8);
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
