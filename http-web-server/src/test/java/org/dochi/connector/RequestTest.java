package org.dochi.connector;

import org.dochi.buffer.Http11InputBufferTest2;
import org.dochi.buffer.HttpClient;
import org.dochi.http.request.data.HttpVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RequestTest extends Http11InputBufferTest2 {
    Request request = new Request(new Connector(HttpVersion.HTTP_1_1));
    private HttpClient httpClient;

    @BeforeEach
    void setRequest() {
        httpClient = new HttpClient(clientConnectedSocket);
        request.setRequest(internalRequest);
    }

    @Test
    void recycle() {
    }

    @Test
    void getPart() {
    }

    @Test
    void getMethod() {
    }

    @Test
    void getRequestURI() {
    }

    @Test
    void getPath() {
    }

    @Test
    void getQueryString() {
    }

    @Test
    void getProtocol() {
    }

    @Test
    void getHeader() {
    }

    @Test
    void getCookie() {
    }

    @Test
    void getContentType() {
    }

    @Test
    void getContentLength() {
    }

    @Test
    void getCharacterEncoding() {
    }

    @Test
    void getParameter() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);

        String message = header + "\r\n" + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));

        assertTrue(inputBuffer.parseHeader(internalRequest));
        assertEquals("john park", request.getParameter("name"));
        assertEquals("1234", request.getParameter("password"));
    }

    @Test
    void getInputStream() {
    }
}