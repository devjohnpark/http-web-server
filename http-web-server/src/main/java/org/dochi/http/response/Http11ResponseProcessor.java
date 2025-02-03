package org.dochi.http.response;

import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.util.DateFormatter;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpResConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Http11ResponseProcessor implements HttpResponseProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11ResponseProcessor.class);
    private final BufferedOutputStream bos;
    private final StatusLine statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
    private final ResponseHeaders headers = new ResponseHeaders();
    private final HttpResConfig httpResConfig;
    private boolean isDefaultDateHeader = true;

    public Http11ResponseProcessor(OutputStream out, HttpResConfig httpResConfig) {
        this.bos = new BufferedOutputStream(out);
        this.httpResConfig = httpResConfig;
    }

    public Http11ResponseProcessor addHeader(String key, String value) {
        this.headers.addHeader(key, value);
        return this;
    }

    public Http11ResponseProcessor addCookie(String cookie) {
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

    public Http11ResponseProcessor addVersion(HttpVersion version) {
        this.statusLine.setVersion(version);
        return this;
    }

    public Http11ResponseProcessor addConnection(boolean isKeepAlive) {
        this.headers.addConnection(isKeepAlive);
        return this;
    }

    public Http11ResponseProcessor addKeepAlive(int timeout, int maxRequests) {
        this.headers.addKeepAlive(timeout, maxRequests);
        return this;
    }

    private void addStatus(HttpStatus status) {
        this.statusLine.setStatus(status);
    }

    private void addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
    }

    private void addContentHeaders(String contentType, int contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
    }

    public void inActiveDateHeader() {
        this.isDefaultDateHeader = false;
    }

    public void activeDateHeader() {
        this.isDefaultDateHeader = true;
    }

    // send하면 clear 가능
    // http api에서 send를 호출안하며 refresh 안됨
    public void refresh() {
        headers.clear();
    }

    public void sendNoContent() throws IOException {
        send(HttpStatus.NO_CONTENT, null, null);
    }

    // 테스트 필요
    public void send(HttpStatus status) throws IOException {
        send(status, new byte[0], null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addStatus(status);
        if (isDefaultDateHeader) {
            addDateHeaders(DateFormatter.getCurrentDate());
        }
        if (body == null) {
            body = new byte[0];
        }
        // 204 No Content일 경우에만, contentType과 content length 생략
        if (status != HttpStatus.NO_CONTENT) {
            addContentHeaders(contentType, body.length);
        }
        writeHttpResMessage(body);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        if (errorMessage == null) {
            errorMessage = status.getDescription();
        }
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType("UTF-8"));
    }

    private void writeHttpResMessage(byte[] body) throws IOException {
        writeStatusLine();
        writeHeaders();
        writeBody(body);
    }

    private void writeStatusLine() throws IOException {
        bos.write(statusLine.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void writeHeaders() throws IOException {
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            bos.write(headerLine.getBytes(StandardCharsets.UTF_8));
        }
        bos.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeBody(byte[] body) throws IOException {
        if (body != null) {
            bos.write(body, 0, body.length);
        }
        bos.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장 (DataOutputStream은 8바이트의 버퍼 하나 존재)
    }
}