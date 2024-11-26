package org.doci.http.response;

import org.doci.http.request.HttpVersion;
import org.doci.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.doci.util.DateFormatter;

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
        this.headers.addHeader(ResponseHeaders.SERVER, "doci");
        this.headers.addHeader(ResponseHeaders.DATE, DateFormatter.getCurrentDate());
        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
        this.headers.addHeader(ResponseHeaders.CONTENT_LENGTH, contentLength != null ? String.valueOf(contentLength) : null);
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
        dos.writeBytes("\r\n");
        dos.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장 (DataOutputStream은 8바이트의 버퍼 하나 존재)
    }
}