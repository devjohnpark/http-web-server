package org.dochi.http.response;

import org.dochi.http.request.HttpVersion;
import org.dochi.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dochi.util.DateFormatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final BufferedOutputStream bos;
    private final StatusLine statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
    private final ResponseHeaders headers = new ResponseHeaders();
    private boolean isDefaultDateHeader = true;

    public HttpResponse(OutputStream out) {
        this.bos = new BufferedOutputStream(out);
    }

    public HttpResponse addHeader(String key, String value) {
        this.headers.addHeader(key, value);
        return this;
    }

    public HttpResponse addCookie(String cookie) {
        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
        return this;
    }

    public HttpResponse addVersion(HttpVersion version) {
        this.statusLine.setVersion(version);
        return this;
    }

    public HttpResponse addConnection(boolean isKeepAlive) {
        this.headers.addConnection(isKeepAlive);
        return this;
    }

    public HttpResponse addKeepAlive(int timeout, int maxRequests) {
        this.headers.addKeepAlive(timeout, maxRequests);
        return this;
    }

    private void addStatus(HttpStatus status) {
        this.statusLine.setStatus(status);
    }

    private void addDateHeaders(String date) {
        this.headers.addHeader(ResponseHeaders.DATE, date);
    }

    private void addContentHeaders(String contentType, Integer contentLength) {
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addContentLength(contentLength);
    }

    public void inActiveDateHeader() {
        this.isDefaultDateHeader = false;
    }

    public void activeDateHeader() {
        this.isDefaultDateHeader = true;
    }

    public void refresh() {
        headers.clearHeaders();
    }

    public void send(HttpStatus status) throws IOException {
        send(status, null, null);
    }

    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
        addStatus(status);
        if (isDefaultDateHeader) {
            addDateHeaders(DateFormatter.getCurrentDate());
        }
        if (body != null) {
            addContentHeaders(contentType, body.length);
        }
        writeHttpResMessage(body);
    }

    public void sendError(HttpStatus status) throws IOException {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) throws IOException {
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getMimeType());
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
        // \r\n\r\n은 헤더와 본문을 구분하는 구분자이다. 이 구분자가 body 끝에 추가되면, 헤더만 응답한 경우가 된다. 이는 브라우저는 이를 "응답이 끝났고 새로운 요청을 시작할 수 있다"는 신호로 해석할 수 있다.
        //  그래서 만약 응답 본문 끝에 \r\n\r\n이 추가되면, 브라우저는 Connection을 닫았다고 판단하고, 이후 요청에서 새로운 소켓 연결을 시도한다.
//       bos.write("\r\n".getBytes(StandardCharsets.UTF_8));
        bos.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장 (DataOutputStream은 8바이트의 버퍼 하나 존재)
    }

}