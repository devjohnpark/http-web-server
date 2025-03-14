package org.dochi.http.response;

import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.util.DateFormatter;
import org.dochi.webresource.ResourceType;
import org.dochi.webresource.SplitFileResource;
import org.dochi.webserver.config.HttpResConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Http11ResponseProcessor implements HttpResponseProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11ResponseProcessor.class);
    private final BufferedOutputStream bos;
    private final StatusLine statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
    private final ResponseHeaders headers = new ResponseHeaders();
    private final HttpResConfig httpResConfig;
    private boolean isDateHeader = true;

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

    public void addStatus(HttpStatus status) {
        this.statusLine.setStatus(status);
    }

    public void addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
    }

    public void addContentHeaders(String contentType, int contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
    }

    public void inActiveDateHeader() {
        this.isDateHeader = false;
    }

    public void activeDateHeader() {
        this.isDateHeader = true;
    }

    // send하면 clear 가능
    // http api에서 send를 호출안하며 refresh 안됨
    public void recycle() {
        headers.clear();
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

    private void writeMessage(byte[] body) throws IOException {
        writeStatusLine();
        writeHeaders();
        writeBody(body);
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


    // prevention using buffer for memory rapidly increment but maintain sending speed.
    public void sendSplitFile(HttpStatus status, SplitFileResource splitFileResource) throws IOException {
        addDefaultHeader(status, (int) splitFileResource.getFileSize(), splitFileResource.getContentType(null));
        writeMessage(null);
        int bytesRead;
        final byte[] buffer = new byte[8192];
        try(InputStream in = splitFileResource.getInputStream()) {
            // byte[]를 생성하는 대신 BufferedOutputStream 자식 클래스를 작성해서 buf 넘긴다.
            while ((bytesRead = in.read(buffer)) != -1) {
                try {
                    bos.write(buffer, 0, bytesRead);
                    bos.flush(); // 즉시 TCP 버퍼로 전달 (시스템 콜 비용 발생)
                } catch (SocketException e) {
                    log.debug("Send split file failed: {}", e.getMessage());
                    throw e; // 네트워크 오류 전파
                }
            }
        }
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
        bos.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
    }

    public OutputStream getOutputStream() {
        return bos;
    }

    public ResponseHeaders getHeaders() { return headers; }
}