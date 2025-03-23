package org.dochi.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Http11InputBufferTest2 extends BioSocketWrapperTest {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBufferTest2.class);
    private final Request request = new Request();
    private final Http11InputBuffer inputBuffer = new Http11InputBuffer(request, 8912);

    @BeforeEach
    void setUp() {
        inputBuffer.recycle();
        inputBuffer.init(serverConnectedSocket);
    }

    @Test
    void valid_request1() throws IOException {
        String message = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 11\r\n\r\n";
        clientBuffer = message.getBytes(StandardCharsets.ISO_8859_1);
        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length);
        assertTrue(inputBuffer.parseHeader());
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.version().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
        assertEquals("text/plain; charset=UTF-8", request.headers().getHeader("Content-Type"));
        assertEquals("11", request.headers().getHeader("Content-Length"));
    }

    @Test
    void valid_request2() throws IOException {
        String message = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 11\r\n\r\n";
        clientBuffer = message.getBytes(StandardCharsets.ISO_8859_1);
        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length);
        assertTrue(inputBuffer.parseHeader());
        assertEquals("GET", request.method().toString());
        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
        assertEquals("HTTP/1.1", request.version().toString());
        assertEquals("keep-alive", request.headers().getHeader("Connection"));
        assertEquals("text/plain; charset=UTF-8", request.headers().getHeader("Content-Type"));
        assertEquals("11", request.headers().getHeader("Content-Length"));
    }
}



