//package org.dochi.buffer;
//
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.http.response.processor.HttpResponseProcessor;
//import org.dochi.http.response.HttpStatus;
//import org.dochi.http.response.ResponseHeaders;
//import org.dochi.http.response.StatusLine;
//import org.dochi.http.util.DateFormatter;
//import org.dochi.webresource.ResourceType;
//import org.dochi.webresource.SplitFileResource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.BufferedSocketInputStream;
//import java.io.OutputStream;
//import java.net.SocketException;
//import java.nio.charset.StandardCharsets;
//import java.util.Set;
//
//// write 버퍼도 동기화 로직을 없는 버퍼링 출력 객체가 속도가 빠름
//public class Http11ResponseProcessor implements HttpResponseProcessor {
//    private static final Logger log = LoggerFactory.getLogger(Http11ResponseProcessor.class);
//    private SocketWrapperBase<?> socketWrapper;
//    private final StatusLine statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
//    private final ResponseHeaders headers = new ResponseHeaders();
//    private final StringBuilder headerMessage = new StringBuilder();
//
////    private final HttpResConfig httpResConfig;
//    private boolean isDateHeader = true;
//
//    public Http11ResponseProcessor() {
////        this.socketWrapper = socketWrapper;
////        this.httpResConfig = httpResConfig;
//    }
//
//    public void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
//        this.socketWrapper = socketWrapper;
//    }
//
//    public Http11ResponseProcessor addHeader(String key, String value) {
//        this.headers.addHeader(key, value);
//        return this;
//    }
//
//    public Http11ResponseProcessor addCookie(String cookie) {
//        this.headers.addHeader(ResponseHeaders.SET_COOKIE, cookie);
//        return this;
//    }
//
//    public Http11ResponseProcessor addVersion(HttpVersion version) {
//        this.statusLine.setVersion(version);
//        return this;
//    }
//
//    public Http11ResponseProcessor addConnection(boolean isKeepAlive) {
//        this.headers.addConnection(isKeepAlive);
//        return this;
//    }
//
//    public Http11ResponseProcessor addKeepAlive(int timeout, int maxRequests) {
//        this.headers.addKeepAlive(timeout, maxRequests);
//        return this;
//    }
//
//    public void addStatus(HttpStatus status) {
//        this.statusLine.setStatus(status);
//    }
//
//    public void addDateHeaders(String date) {
//        this.headers.addHeader(ResponseHeaders.DATE, date);
//    }
//
//    public void addContentHeaders(String contentType, int contentLength) {
//        this.headers.addHeader(ResponseHeaders.CONTENT_TYPE, contentType);
//        this.headers.addContentLength(contentLength);
//    }
//
//    public void inActiveDateHeader() {
//        this.isDateHeader = false;
//    }
//
//    public void activeDateHeader() {
//        this.isDateHeader = true;
//    }
//
//    // send하면 clear 가능
//    // http api에서 send를 호출안하며 refresh 안됨
//    public void recycle() {
//        headers.clear();
//    }
//
//    public void sendNoContent() throws IOException {
//        send(HttpStatus.NO_CONTENT, null, null);
//    }
//
//    // 테스트 필요
//    public void send(HttpStatus status) throws IOException {
////        send(status, new byte[0], null);
//        send(status, null, null);
//    }
//
//    public void send(HttpStatus status, byte[] body, String contentType) throws IOException {
//        addDefaultHeader(status, getContentLength(body), contentType);
//        writeMessage(body);
//    }
//
//    public void sendError(HttpStatus status) throws IOException {
//        sendError(status, status.getDescription());
//    }
//
//    public void sendError(HttpStatus status, String errorMessage) throws IOException {
//        if (errorMessage == null) {
//            errorMessage = status.getDescription();
//        }
//        headers.addConnection(false);
//        send(status, errorMessage.getBytes(), ResourceType.TEXT.getContentType(null));
//    }
//
//    @Override
//    public OutputStream getOutputStream() {
//        return null;
//    }
//
//    public ResponseHeaders getHeaders() { return headers; }
//
//    private static int getContentLength(byte[] body) {
//        int contentLength = 0;
//        if (body != null) {
//            contentLength = body.length;
//        }
//        return contentLength;
//    }
//
//    private void addDefaultHeader(HttpStatus status, int contentLength, String contentType) {
//        addStatus(status);
//        if (isDateHeader) {
//            addDateHeaders(DateFormatter.getCurrentDate());
//        }
//        // NO_CONTENT면, Content_Length도 없어야함
//        if (status != HttpStatus.NO_CONTENT) {
//            addContentHeaders(contentType, contentLength);
//        }
////        // 204 No Content일 경우에만, contentType과 content length 생략
////        if (status != HttpStatus.NO_CONTENT || body != null) {
////            addContentHeaders(contentType, contentLength);
////        }
//    }
//
//    private void writeMessage(byte[] body) throws IOException {
////        writeStatusLine(); //
////        writeHeaders();
//        writeHeader();
//        writeBody(body);
//    }
//
//
//
//
//    // prevention using buffer for memory rapidly increment but maintain sending speed.
//    public void sendSplitFile(HttpStatus status, SplitFileResource splitFileResource) throws IOException {
//        addDefaultHeader(status, (int) splitFileResource.getFileSize(), splitFileResource.getContentType(null));
//        writeMessage(null);
//        int bytesRead;
//        final byte[] buffer = new byte[8192];
//        try(BufferedSocketInputStream in = splitFileResource.getInputStream()) {
//            // byte[]를 생성하는 대신 BufferedOutputStream 자식 클래스를 작성해서 buf 넘긴다.
//            while ((bytesRead = in.read(buffer)) != -1) {
//                try {
//                    socketWrapper.write(buffer, 0, bytesRead);
//                    socketWrapper.flush(); // 즉시 TCP 버퍼로 전달 (시스템 콜 비용 발생)
//                } catch (SocketException e) {
//                    log.debug("Send split file failed: {}", e.getMessage());
//                    throw e; // 네트워크 오류 전파
//                }
//            }
//        }
//    }
//
//    private void writeHeader() throws IOException {
//        headerMessage.setLength(0);
//        headerMessage.append(statusLine.toString());
//        Set<String> keys = headers.getHeaders().keySet();
//        for (String key: keys) {
//            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
//            headerMessage.append(headerLine);
//        }
//        headerMessage.append("\r\n");
//        byte[] header = headerMessage.toString().getBytes(StandardCharsets.UTF_8);
//        socketWrapper.write(header, 0, header.length);
//    }
//
////    private void writeStatusLine() throws IOException {
////        socketWrapper.write(statusLine.toString().getBytes(StandardCharsets.UTF_8));
////    }
////
////    private void writeHeaders() throws IOException {
////        Set<String> keys = headers.getHeaders().keySet();
////        for (String key: keys) {
////            String headerLine = key + ": " + headers.getHeaders().get(key) + "\r\n";
//////            socketWrapper.write(headerLine.getBytes(StandardCharsets.UTF_8));
////            socketWrapper.write(headerLine.getBytes(StandardCharsets.UTF_8));
////        }
////        socketWrapper.write("\r\n".getBytes(StandardCharsets.UTF_8));
////    }
//
//    private void writeBody(byte[] body) throws IOException {
//        if (body != null) {
//            socketWrapper.write(body, 0, body.length);
//        }
//        socketWrapper.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
//    }
//
////    public OutputStream getOutputStream() {
////        return socketWrapper;
////    }
//}