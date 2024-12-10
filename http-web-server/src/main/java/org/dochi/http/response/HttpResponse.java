package org.dochi.http.response;

import org.dochi.http.request.HttpVersion;
import org.dochi.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dochi.util.DateFormatter;

import java.io.*;
import java.util.Set;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final DataOutputStream dos;
    private final StatusLine statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
    private final ResponseHeaders headers = new ResponseHeaders();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
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
        this.headers.addHeader(ResponseHeaders.CONNECTION, isKeepAlive ? "keep-alive" : "close");
        return this;
    }

    public void send(HttpStatus status) {
        send(status, null, null);
    }

    // writeHttpResMessage에서 클라이언트와 연결 끊기면 IOException의 하위 클래스인 SocketException 처리해야한다.
    public void send(HttpStatus status, byte[] body, String contentType) {
        addStatus(status);
        addHeaders(contentType, body != null ? body.length : null);
        writeHttpResMessage(body);
    }

    public void sendError(HttpStatus status) {
        sendError(status, status.getDescription());
    }

    public void sendError(HttpStatus status, String errorMessage) {
        send(status, errorMessage.getBytes(), ResourceType.TEXT.getMimeType());
    }

    private void addStatus(HttpStatus status) {
        this.statusLine.setStatus(status);
    }

    private void addHeaders(String contentType, Integer contentLength) {
        this.headers.addHeader(ResponseHeaders.SERVER, "dochi");
        this.headers.addHeader(ResponseHeaders.DATE, DateFormatter.getCurrentDate());
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addHeader(ResponseHeaders.CONTENT_LENGTH, contentLength != null ? String.valueOf(contentLength) : null);
        this.headers.addHeader(ResponseHeaders.CONNECTION, "keep-alive");
    }

    private void writeHttpResMessage(byte[] body) {
        try {
            writeStatusLine();
            writeHeaders();
            writeBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeStatusLine() throws IOException {
        dos.writeBytes(statusLine.toString());
    }

    private void writeHeaders() throws IOException {
        Set<String> keys = headers.getHeaders().keySet();
        for (String key: keys) {
            dos.writeBytes(key + ": " + headers.getHeaders().get(key) + "\r\n");
        }
        dos.writeBytes("\r\n");
    }

    private void writeBody(byte[] body) throws IOException {
        if (body != null) {
            dos.write(body, 0, body.length);
        }
        // \r\n\r\n은 헤더와 본문을 구분하는 구분자이다. 이 구분자가 body 끝에 추가되면, 헤더만 응답한 경우가 된다. 이는 브라우저는 이를 "응답이 끝났고 새로운 요청을 시작할 수 있다"는 신호로 해석할 수 있다.
        //  그래서 만약 응답 본문 끝에 \r\n\r\n이 추가되면, 브라우저는 Connection을 닫았다고 판단하고, 이후 요청에서 새로운 소켓 연결을 시도한다.
//        dos.writeBytes("\r\n");
        dos.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장 (DataOutputStream은 8바이트의 버퍼 하나 존재)
    }
}