package org.dochi.buffer;

import org.dochi.internal.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Http11InputBufferTest2 extends BioSocketWrapperTest {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBufferTest2.class);
//    private final Http11InputBuffer inputBuffer = new Http11InputBuffer(request, 8912);
    protected final int headerMaxSize = 400;
    protected final Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);
    protected final Request internalRequest = new Request(inputBuffer);
    private HttpClient httpClient;

    @BeforeEach
    void init() {
        internalRequest.recycle();
        inputBuffer.init(serverConnectedSocket);
        httpClient = new HttpClient(clientConnectedSocket);
    }

    @Test
    void valid_get() throws IOException {
//        doHttp11Request("GET /user HTTP/1.1\r\nConnection: keep-alive\r\n", null);
        httpClient.doRequest("GET /user HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(internalRequest));
        assertEquals("GET", internalRequest.method().toString());
        assertEquals("/user", internalRequest.path().toString());
        assertEquals("", internalRequest.queryString().toString());
        assertEquals("HTTP/1.1", internalRequest.protocol().toString());
        assertEquals("keep-alive", internalRequest.headers().getHeader("Connection"));
    }

    @Test
    void valid_get_querystring() throws IOException {
//        doHttp11Request("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\n", null);
        httpClient.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(internalRequest));
        assertEquals("GET", internalRequest.method().toString());
        assertEquals("/user?name=john%20park&password=1234", internalRequest.requestURI().toString());
        assertEquals("/user", internalRequest.path().toString());
        assertEquals("name=john%20park&password=1234", internalRequest.queryString().toString());
        assertEquals("HTTP/1.1", internalRequest.protocol().toString());
        assertEquals("keep-alive", internalRequest.headers().getHeader("Connection"));
    }

    @Test
    void valid_post_form_urlencoded() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader(internalRequest));

        assertEquals("POST", internalRequest.method().toString());
        assertEquals("/user", internalRequest.requestURI().toString());
        assertEquals("HTTP/1.1", internalRequest.protocol().toString());
        assertEquals("keep-alive", internalRequest.headers().getHeader("Connection"));
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", internalRequest.getContentType());
        assertEquals("utf-8", internalRequest.getCharacterEncoding());
        assertEquals(contentLength, internalRequest.getContentLength());
    }
}



