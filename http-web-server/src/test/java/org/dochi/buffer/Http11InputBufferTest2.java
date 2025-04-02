package org.dochi.buffer;

import org.dochi.buffer.internal.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Http11InputBufferTest2 extends BioSocketWrapperTest {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBufferTest2.class);
//    private final Http11InputBuffer inputBuffer = new Http11InputBuffer(request, 8912);
    protected final int headerMaxSize = 400;
    protected final Http11InputBuffer inputBuffer = new Http11InputBuffer(headerMaxSize);
    protected final Request request = new Request(inputBuffer);

    @BeforeEach
    void init() {
        request.recycle();
//        inputBuffer.recycle();
//        request.setInputBuffer(inputBuffer);
        inputBuffer.init(serverConnectedSocket);
    }


    @Test
    void valid_get() throws IOException {
        sendHttp11TextMessageToServer("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive", null);
        assertTrue(inputBuffer.parseHeader(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
    }

    @Test
    void valid_post() throws IOException {
        sendHttp11TextMessageToServer("GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 11", "Hello world");
        assertTrue(inputBuffer.parseHeader(request));
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.protocol().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
        assertEquals("text/plain; charset=UTF-8", request.headers().getHeader("Content-Type"));
        assertEquals("11", request.headers().getHeader("Content-Length"));
    }
}



