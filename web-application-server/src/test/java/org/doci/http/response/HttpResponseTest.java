package org.doci.http.response;

import org.doci.util.DateFormatter;
import org.doci.webresources.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {
    // add headers -> send -> get data of OutputStream -> check
    // OutputStream.toString(): ByteArrayOutputStream 클래스의 메서드로, 스트림에 쓰여진 바이트 데이터를 문자열로 변환하여 반환
    private ByteArrayOutputStream outputStream;
    private HttpResponse response;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        response = new HttpResponse(outputStream);
    }

    @Test
    void send_status() {
        response.send(HttpStatus.OK);

        String result = outputStream.toString();
        assertTrue(result.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(result.contains("Server: doci\r\n"));
        assertTrue(result.contains("Date: "));
        assertTrue(result.endsWith("\r\n\r\n"));
    }


    @Test
    void send_body() {
        String body = "Hello, World!";
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        response.send(HttpStatus.OK, bodyBytes, ResourceType.TEXT.getMimeType());

        String result = outputStream.toString();
        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getMimeType()));
        assertTrue(result.contains("Content-Length: " + bodyBytes.length));
        assertTrue(result.contains(body));
    }

    @Test
    void sendError() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        response.sendError(status);

        String result = outputStream.toString();
        assertTrue(result.startsWith("HTTP/1.1 404 Not Found\r\n"));
        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getMimeType()));
        assertTrue(result.contains("Content-Length: " + status.getDescription().length()));
        assertTrue(result.contains(HttpStatus.NOT_FOUND.getDescription()));
    }

    @Test
    void sendError_errorMessage() {
        String errorMessage = "error message";

        response.sendError(HttpStatus.BAD_REQUEST, errorMessage);

        String result = outputStream.toString();
        assertTrue(result.contains("Content-Type: " + ResourceType.TEXT.getMimeType()));
        assertTrue(result.contains("Content-Length: " + errorMessage.length()));
        assertTrue(result.contains(errorMessage));
    }

    @Test
    void send_body_null() {
        response.send(HttpStatus.OK, null, null);

        String result = outputStream.toString();
        assertFalse(result.contains("Content-Type:"));
        assertFalse(result.contains("Content-Length:"));
    }
}