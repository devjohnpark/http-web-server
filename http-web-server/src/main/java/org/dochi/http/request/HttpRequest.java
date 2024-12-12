package org.dochi.http.request;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.dochi.webresource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private BufferedReader br;
    private RequestLine requestLine;
    private RequestHeaders headers;
    private RequestParameters parameters;

    public HttpRequest(InputStream in) {
        this.br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public boolean prepareHttpRequest() throws IOException {
        try {
            requestLine = setRequestLine();
            headers = setHeaders();
            parameters = setParameters();
            return true;
        } catch (IllegalStateException e) {
            log.debug(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }
        return false;
    }


    private RequestLine setRequestLine() throws IOException {
        String line = br.readLine();
        if (line == null) {
            // readLine(): null if the end of the stream has been reached without reading any character
            // 클라이언트 종료 -> 소켓 닫힘 -> OS가 소켓과 연결된 파일 디스크립터에 EOF 상태 설정 -> java 애플리케이션이 스트림을 통해 EOF를 감지(네이티브 코드에 의해 처리됨) -> EOF에 도달하여 read() 메서드 -1 반환 -> readLine() null 반환
            // TCP/IP 통신에서 연결 종료는 4-way handshake를 통해 이루어진다.
            // 연결 종료 시 소켓 스트림에 파일의 끝인 EOF(End of File) 마커가 전달된다.
            // 네트워크 소켓의 경우, EOF는 클라이언트와의 연결이 완전히 종료되었음을 나타낸다.
            // 즉, 클라이언트가 데이터 전송을 완료하고 연결을 닫았다는 의미이다.
            throw new IllegalStateException("Request line is null: client closed connection");
        }
        try {
            return RequestLine.createFromRequestLine(line);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request line: " + line);
        }
    }

    private RequestHeaders setHeaders() throws IOException {
        String line;
        RequestHeaders headers = new RequestHeaders();
        while (!(line = br.readLine()).isEmpty()) {
            headers.addHeader(line);
        }
        return headers;
    }

    private RequestParameters setParameters() throws IOException {
        RequestParameters parameters = new RequestParameters();
        parameters.addRequestParameters(requestLine.getQueryString());
        if (ResourceType.URL.getMimeType().equals(headers.getContentType())) {
            parameters.addRequestParameters(getAllBodyAsString());
        }
        return parameters;
    }

    // body에 대한 정확한 content-length 받아야, HTTP 트랜잭션이 끝나는 시점에 기존 메시지의 끝과 새로운 메세지의 시작점을 정확히 알수있다.
    public String getAllBodyAsString() throws IOException {
        int contentLength = headers.getContentLength();
        char[] body = new char[contentLength];
        int actualLength = br.read(body, 0, contentLength);
        return String.copyValueOf(body, 0, actualLength);
    }

    public byte[] getBodyAsBytes() throws IOException {
        return getAllBodyAsString().getBytes(StandardCharsets.UTF_8);
    }

    public BufferedReader getBufferedReader() { return br; }

    public HttpMethod getMethod() { return requestLine.getMethod(); }

    public String getPath() { return requestLine.getPath(); }

    public String getRequestParameter(String key) { return parameters.getParameter(key); }

    public HttpVersion getHttpVersion() { return requestLine.getVersion(); }

    public String getHeader(String key) { return headers.getHeader(key); }

//    public String getCookie() { return headers.getCookie(); }
//
//    public String getContentType() { return headers.getContentType(); }
//
//    public int getContentLength() { return headers.getContentLength(); }
//
//    public String getConnection() { return headers.getConnection(); }
}
