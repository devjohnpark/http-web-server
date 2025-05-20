//package org.dochi.buffer;
//
//import org.dochi.external.InternalInputStream;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.*;
//
//class InternalBufferedInputStreamTest extends Http11InputBufferTest {
//    private static final Logger log = LoggerFactory.getLogger(InternalBufferedInputStreamTest.class);
//    InternalInputStream internalBufferedInputStream = new InternalInputStream(inputBuffer, 8192);
//    private HttpClient httpClient = new HttpClient(clientConnectedSocket);
//
////    @BeforeEach
////    void setUp() {
////        httpClient = new HttpClient(clientConnectedSocket);
////    }
//
//    @BeforeEach
//    void recycle() {
//        httpClient = new HttpClient(clientConnectedSocket);
//        internalBufferedInputStream.recycle();
//    }
//
//    // InputBuffer로 헤더를 파싱한 이후에 바디를 읽는 것을 테스트
//
//    // InternalInputStream 내의 InputBuffer의 ByteBuffer만 사용
//    @Test
//    void read() throws IOException {
////        valid_post_form_urlencoded(); // 의존되어 있음 코드 변경 필요
//        String body = "name=john%20park&password=1234";
//        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
//        int contentLength = buf.length;
//        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
//        String message = header + "\r\n" + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//        assertTrue(inputBuffer.parseHeader(internalRequest));
//        assertEquals(internalBufferedInputStream.read(), 'n');
//    }
//
//    @Test
//    void read_buffer() throws IOException {
//        String body = "name=john%20park&password=1234";
//        byte[] buf = new byte[1024];
//        int contentLength = buf.length;
//        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
//        String message = header + "\r\n" + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//        assertTrue(inputBuffer.parseHeader(internalRequest));
//        int n = internalBufferedInputStream.read(buf);
//        assertArrayEquals(Arrays.copyOf(buf, n), body.getBytes(StandardCharsets.ISO_8859_1));
//    }
//
//    // blocking 발생
//    @Test
//    void read_buffer_length() throws IOException {
//        String body = "name=john%20park&password=1234";
//        byte[] buf = new byte[1024];
//        int contentLength = buf.length;
//        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
//        String message = header + "\r\n" + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//        assertTrue(inputBuffer.parseHeader(internalRequest));
//        int n = internalBufferedInputStream.read(buf, 0, 2);
//        assertArrayEquals(Arrays.copyOf(buf, n), "na".getBytes(StandardCharsets.ISO_8859_1));
//    }
//
//    @Test
//    void read_from_input_stream_buffer() throws IOException {
//        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
//        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
//        int contentLength = buf.length;
//        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
//
//        String message = header + "\r\n" + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//        assertTrue(inputBuffer.parseHeader(internalRequest));
//
//        for (byte b : buf) {
//            assertEquals(internalBufferedInputStream.read(), b);
//        }
//    }
//
//    @Test
//    void read_buffer_from_input_stream_buffer() throws IOException {
//        String body = "Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world";
//        byte[] buf = body.getBytes(StandardCharsets.ISO_8859_1);
//        int contentLength = buf.length;
//        String header = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\n" + String.format("Content-Length: %d\r\n", contentLength);
//
//        String message = header + "\r\n" + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//        assertTrue(inputBuffer.parseHeader(internalRequest));
//
//        byte[] bodyBuf = new byte[contentLength];
//        int n = 0;
//        while (n < contentLength) {
//            n += internalBufferedInputStream.read(bodyBuf, n, contentLength - n);
//        }
//        assertArrayEquals(bodyBuf, body.getBytes(StandardCharsets.ISO_8859_1));
//    }
//}