package org.dochi.buffer;

import org.dochi.buffer.internal.InternalInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

class InternalInputStreamTest extends Http11InputBufferTest2 {
    private static final Logger log = LoggerFactory.getLogger(InternalInputStreamTest.class);
    InternalInputStream internalInputStream = new InternalInputStream(inputBuffer, 8192);

    @BeforeEach
    void recycle() {
        internalInputStream.recycle();
    }

    // InputBuffer로 헤더를 파싱한 이후에 바디를 읽는 것을 테스트

    // InternalInputStream 내의 InputBuffer의 ByteBuffer만 사용
    @Test
    void read() throws IOException {
        valid_post();
        assertEquals(internalInputStream.read(), 'H');
    }

    @Test
    void read_buffer() throws IOException {
        valid_post();
        byte[] buf = new byte[30];
        int n = internalInputStream.read(buf);
        assertArrayEquals(Arrays.copyOf(buf, n), "Hello world".getBytes(StandardCharsets.ISO_8859_1));
    }

    // blocking 발생
    @Test
    void read_buffer_length() throws IOException {
        valid_post();
        byte[] buf = new byte[30];
        int n = internalInputStream.read(buf, 0, 2);
        assertArrayEquals(Arrays.copyOf(buf, n), "He".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void read_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d", contentLength);

        sendHttp11TextMessageToServer(header, body);
        assertTrue(inputBuffer.parseHeader(request));

        for (byte b : buf) {
            assertEquals(internalInputStream.read(), b);
        }
    }

    @Test
    void read_buffer_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d", contentLength);

        sendHttp11TextMessageToServer(header, body);
        assertTrue(inputBuffer.parseHeader(request));

        byte[] bodyBuf = new byte[contentLength];
        int n = 0;
        while (n < contentLength) {
            n += internalInputStream.read(bodyBuf, n, contentLength - n);
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }
}