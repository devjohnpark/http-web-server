//package org.dochi.connector;
//
//import org.dochi.buffer.Http11InputBufferTest;
//import org.dochi.buffer.HttpClient;
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.inputbuffer.connector.Connector;
//import org.dochi.inputbuffer.connector.Request;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class RequestTest extends Http11InputBufferTest {
//    Request request = new Request(new Connector(HttpVersion.HTTP_1_1));
//    private HttpClient httpClient;
//
//    @BeforeEach
//    void setInternalRequest() {
//        httpClient = new HttpClient(clientConnectedSocket);
//        request.setInternalRequest(super.request);
//    }
//
//    @Test
//    void getPart() throws IOException {
//        String body =
//                "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
//                        + "Content-Disposition: form-data; name=\"username\"\r\n"
//                        + "\r\n"
//                        + "john\r\n"
//                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
//                        + "Content-Disposition: form-data; name=\"age\"\r\n"
//                        + "\r\n"
//                        + "4\r\n"
//                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n"
//                        + "Content-Disposition: form-data; name=\"file\"; filename=\"imageFile.png\"\r\n"
//                        + "Content-Type: image/png\r\n"
//                        + "\r\n"
//                        + "21312445321553451234213412341234234124234\r\n"
//                        + "------WebKitFormBoundarylwQGqAAJBIOZfE7B--\r\n";
//        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
//        int contentLength = buf.length; // 30
//        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: multipart/form-data; boundary=----WebKitFormBoundarylwQGqAAJBIOZfE7B\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
//
//        String message = header + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.UTF_8));
//
//        assertTrue(inputBuffer.parseHeader());
////        assertEquals("1234", request.getParameter("password"));
////        assertEquals("john park", request.getParameter("name"));
//
//        assertThat(request.getPart("username").getContent()).isEqualTo("john".getBytes(StandardCharsets.UTF_8));
//        assertThat(request.getPart("age").getContent()).isEqualTo("4".getBytes(StandardCharsets.UTF_8));
//        assertThat(request.getPart("file").getContent()).isEqualTo("21312445321553451234213412341234234124234".getBytes(StandardCharsets.UTF_8));
////        request.recycle();
////        assertNull(request.getPart("age").getContent());
//
//    }
//
//    @Test
//    void getMethod() {
//    }
//
//    @Test
//    void getRequestURI() {
//    }
//
//    @Test
//    void getRequestPath() {
//    }
//
//    @Test
//    void getQueryString() {
//    }
//
//    @Test
//    void getProtocol() {
//    }
//
//    @Test
//    void getHeader() {
//    }
//
//    @Test
//    void getCookie() {
//    }
//
//    @Test
//    void getContentType() {
//    }
//
//    @Test
//    void getContentLength() {
//    }
//
//    @Test
//    void getCharacterEncoding() {
//    }
//
//    @Test
//    void getParameter() throws IOException {
//        String body = "name=john%20park&password=1234";
//        byte[] buf = body.getBytes(StandardCharsets.UTF_8);
//        int contentLength = buf.length; // 30
//        String header = "POST /user HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded; charset=utf-8\r\n" + String.format("Content-Length: %d\r\n\r\n", contentLength);
//
//        String message = header + body;
//        httpClient.doRequest(message.getBytes(StandardCharsets.ISO_8859_1));
//
//        assertTrue(inputBuffer.parseHeader());
//        assertEquals("1234", request.getParameter("password"));
//        assertEquals("john park", request.getParameter("name"));
//    }
//
//    @Test
//    void getInputStream() {
//    }
//}