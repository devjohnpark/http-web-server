package org.dochi.connector;

import org.dochi.http.response.BufferedOutputStream;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.response.ResponseHeaders;
import org.dochi.http.util.DateFormatter;
import org.dochi.external.HttpExternalResponse;
import org.dochi.http.data.HttpVersion;
import org.dochi.webserver.config.HttpResConfig;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.dochi.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Http11ResponseHandler implements ResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(Http11ResponseHandler.class);
    protected HttpVersion version = HttpVersion.HTTP_1_1;
    protected HttpStatus status = HttpStatus.OK;
    protected final ResponseHeaders headers = new ResponseHeaders();
    protected final HttpResConfig httpResConfig;
    private boolean isDateHeader = true;
    private boolean isCommitted = false;
    protected final BufferedOutputStream bos;

    public Http11ResponseHandler(HttpResConfig httpResConfig) {
        this.bos = new BufferedOutputStream();
        this.httpResConfig = httpResConfig;
    }

    @Override
    public void init(SocketWrapperBase<?> socketWrapper) {
        this.bos.init(socketWrapper);
    }

    @Override
    public void recycle() {
        headers.clear();
        bos.recycle();
        isDateHeader = true;
        isCommitted = false;
        version = HttpVersion.HTTP_1_1;
        status = HttpStatus.OK;
    }

    public HttpExternalResponse addVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public HttpExternalResponse addStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpExternalResponse addHeader(String key, String value) {
        this.headers.addHeader(key, value);
        return this;
    }

    public HttpExternalResponse addCookie(String cookie) {
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

    public HttpExternalResponse addConnection(boolean isKeepAlive) {
        this.headers.addConnection(isKeepAlive);
        return this;
    }

    public HttpExternalResponse addKeepAlive(int timeout, int maxRequests) {
        this.headers.addKeepAlive(timeout, maxRequests);
        return this;
    }

    public HttpExternalResponse addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
        return this;
    }

    public HttpExternalResponse addContentHeaders(String contentType, int contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
        return this;
    }

    public HttpExternalResponse inActiveDateHeader() {
        this.isDateHeader = false; return this;
    }

    public HttpExternalResponse activeDateHeader() {
        this.isDateHeader = true; return this;
    }

    public void sendNoContent() throws IOException {
        send(HttpStatus.NO_CONTENT, null, null);
    }

    // 테스트 필요
    public void send(HttpStatus status) throws IOException {
//        send(status, new byte[0], null);
        send(status, null, null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addDefaultHeader(status, getContentLength(body), contentType);
        writeMessage(body);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        if (errorMessage == null) {
            errorMessage = status.getDescription();
        }
        headers.addConnection(false);
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType(null));
    }

    private static int getContentLength(byte[] body) {
        int contentLength = 0;
        if (body != null) {
            contentLength = body.length;
        }
        return contentLength;
    }

    private void addDefaultHeader(HttpStatus status, int contentLength, String contentType) {
        addStatus(status);
        if (isDateHeader) {
            addDateHeaders(DateFormatter.getCurrentDate());
        }
        // NO_CONTENT면, Content_Length도 없어야함
        if (status != HttpStatus.NO_CONTENT) {
            addContentHeaders(contentType, contentLength);
        }
//        // 204 No Content일 경우에만, contentType과 content length 생략
//        if (status != HttpStatus.NO_CONTENT || body != null) {
//            addContentHeaders(contentType, contentLength);
//        }
    }
    public OutputStream getOutputStream() {
        return bos.getOutputStream();
    }

    private void writeMessage(byte[] body) throws IOException {
        try {
            writeHeader();
            writePayload(body);
        } catch (IOException e) {
            log.error("Failed to write HTTP Response Message: {}", e.getMessage(), e);
            throw e;
        }
    }

//    protected abstract void writeHeader() throws IOException;
//    protected abstract void writePayload(byte[] body) throws IOException;

    private void writeHeader() throws IOException {
        bos.write(String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage()).getBytes(StandardCharsets.ISO_8859_1));
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
            bos.write(headerLine.getBytes(StandardCharsets.ISO_8859_1));
        }
        bos.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writePayload(byte[] body) throws IOException {
        if (body != null) {
            bos.write(body, 0, body.length);
        }
//        flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
    }

    public void flush() throws IOException {
        if (!isCommitted) {
            bos.flush();
            isCommitted = true;
        }
    }
}
