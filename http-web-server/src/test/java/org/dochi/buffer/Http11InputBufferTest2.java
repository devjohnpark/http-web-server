package org.dochi.buffer;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.internal.Request;
import org.dochi.internal.http11.Http11InputBuffer;
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
    protected final Request request = new Request();
    protected Http11InputBuffer inputBuffer = new Http11InputBuffer(request, headerMaxSize);
//    protected final Request request = new Request(inputBuffer);
    private HttpClient httpClient;

    @BeforeEach
    void init() {
//        internalRequest.recycle();
        inputBuffer.recycle(); // request.recycle() 포함
        request.setInputBuffer(inputBuffer);
        inputBuffer.init(serverConnectedSocket);
        httpClient = new HttpClient(clientConnectedSocket);
    }

    @Test
    void valid_get() throws IOException {
//        doHttp11Request("GET /user HTTP/1.1\r\nConnection: keep-alive\r\n", null);
        httpClient.doRequest("GET /user HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader());
        assertEquals("GET", request.method().toString());
        assertEquals("/user", request.requestPath().toString());
        assertEquals("", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
    }

    @Test
    void valid_get_querystring() throws IOException {
//        doHttp11Request("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\n", null);
        httpClient.doRequest("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader());
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("/user", request.requestPath().toString());
        assertEquals("name=john%20park&password=1234", request.queryString().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
    }

    @Test
    void valid_get_header_size_exceed() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        this.inputBuffer = new Http11InputBuffer(request, header.getBytes(StandardCharsets.ISO_8859_1).length - 1);
        this.init();
        String message = header + body;

        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertThrows(HttpStatusException.class, () -> inputBuffer.parseHeader());
    }

    @Test
    void valid_post_form_urlencoded() throws IOException {
        String body = "/user?name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;

        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(inputBuffer.parseHeader());

        assertEquals("POST", request.method().toString());
        assertEquals("/user", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
        assertEquals("application/x-www-form-urlencoded; charset=utf-8", request.getContentType());
        assertEquals("utf-8", request.getCharacterEncoding());
        assertEquals(contentLength, request.getContentLength());
    }
}



