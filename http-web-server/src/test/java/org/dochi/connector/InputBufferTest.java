package org.dochi.connector;

import org.dochi.buffer.Http11InputBufferTest2;
import org.dochi.buffer.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InputBufferTest extends Http11InputBufferTest2 {
    private static final Logger log = LoggerFactory.getLogger(InputBufferTest.class);
    InputBuffer inputBuffer = new InputBuffer();
    private HttpClient httpClient = new HttpClient(clientConnectedSocket);

//    @BeforeEach
//    void setUp() {
//        httpClient = new HttpClient(clientConnectedSocket);
//    }

    @BeforeEach
    void recycle() {
        httpClient = new HttpClient(clientConnectedSocket);
        inputBuffer.recycle();
        inputBuffer.setRequest(request);
    }

    // InputBuffer로 헤더를 파싱한 이후에 바디를 읽는 것을 테스트

    // InternalInputStream 내의 InputBuffer의 ByteBuffer만 사용
    @Test
    void read() throws IOException {
//        valid_post_form_urlencoded(); // 의존되어 있음 코드 변경 필요
        String body = "name=john%20park&password=1234";
        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
        int contentLength = buf.length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
        String message = header + "\r\n" + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader());
        assertEquals(inputBuffer.read(), 'n');
    }

    @Test
    void read_buffer() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        int headerSize = header.getBytes(StandardCharsets.UTF_8).length;
        System.out.println("header size: " + headerSize); // 133
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader());

        byte[] bodyBuf = new byte[body.length()];
        int j = 0;
        int total = 0;
        while (total < contentLength) {
            int n = inputBuffer.read(buf);
            for (int i = 0; i < n; i++) {
                bodyBuf[j++] = (byte) buf[i];
            }
            total += n;
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }

    // blocking 발생
    @Test
    void read_buffer_length() throws IOException {
        String body = "name=john%20park&password=1234";
        byte[] buf = new byte[1024];
        int contentLength = body.getBytes(StandardCharsets.UTF_8).length;
        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader());
        int n = inputBuffer.read(buf, 0, 2);
        assertArrayEquals(Arrays.copyOf(buf, n), "na".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void read_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader());
        for (byte b : buf) {
            assertEquals(inputBuffer.read(), b);
        }
    }

    @Test
    void read_buffer_from_input_stream_buffer() throws IOException {
        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
        int contentLength = buf.length;
        System.out.println("content size: " + contentLength);
        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
        System.out.println("header size: " + header.getBytes(StandardCharsets.ISO_8859_1).length);

        String message = header + body;
        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(super.inputBuffer.parseHeader());

        byte[] bodyBuf = new byte[contentLength];
        int n = 0;
        while (n < contentLength) {
            n += inputBuffer.read(bodyBuf, n, contentLength - n);
        }
        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
    }
}