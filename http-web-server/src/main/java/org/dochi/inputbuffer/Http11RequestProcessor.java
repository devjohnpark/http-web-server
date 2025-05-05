//package org.dochi.inputbuffer;
//
//import org.dochi.connector.Connector;
//import org.dochi.connector.Request;
//import org.dochi.http.exception.HttpStatusException;
//import org.dochi.http.request.data.HttpMethod;
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.http.request.multipart.Part;
//import org.dochi.http.request.processor.HttpRequestProcessor;
//import org.dochi.internal.http11.Http11InputBuffer;
//import org.dochi.webserver.config.HttpReqConfig;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//public class Http11RequestProcessor implements HttpRequestProcessor {
//    private static final Logger log = LoggerFactory.getLogger(Http11RequestProcessor.class);
////    private final ContentLengthValidator contentLengthValidator;
//    private final Http11InputBuffer inputBuffer;
//    private final org.dochi.internal.Request internalRequest = new org.dochi.internal.Request();
//    private final Request request = new Request(new Connector());
//
//    public Http11RequestProcessor(Http11InputBuffer inputBuffer, HttpReqConfig httpReqConfig) {
////        super(httpReqConfig);
//        this.inputBuffer = inputBuffer;
//        this.internalRequest.setInputBuffer(inputBuffer);
//        this.request.setInternalRequest(this.internalRequest);
////        this.contentLengthValidator = new ContentLengthValidator(sizeMonitor.getContentMonitor());
//    }
//
//    public void setInputBuffer(SocketWrapperBase<?> socketWrapper) {
//        this.inputBuffer.init(socketWrapper);
//    }
//
//    @Override
//    public boolean isPrepareHeader() throws IOException, HttpStatusException {
////        return processRequestLine(sizeMonitor) && processHeaders(sizeMonitor);
//        return inputBuffer.parseHeader();
//    }
//
//    @Override
//    public void recycle() throws IOException {
//        request.recycle();
//        internalRequest.recycle();
//    }
//
//    @Override
//    public org.dochi.http.request.data.Request getParsedRequest() {
//        return null;
//    }
//
////    private boolean processRequestLine(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
////        String requestLine = inputBuffer.readHeader(sizeMonitor);
////        if (isEOFOnCloseWait(requestLine)) {
////            return false;
////        }
////        try {
////            request.metadata().addRequestLine(requestLine);
////            return true;
////        } catch (IllegalArgumentException e) {
////            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
////        }
////    }
//
////    private boolean isEOFOnCloseWait(String requestLine) {
////        return requestLine == null;
////    }
////
////    private boolean processHeaders(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
////        String line;
////        while ((line = inputBuffer.readHeader(sizeMonitor)) != null) {
////            if (line.isEmpty()) {
////                return true;
////            }
////            request.headers().addHeaderLine(line);
////        }
////        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected end of stream while reading HTTP header");
////    }
//
//    @Override
//    public byte[] getAllBody() throws IOException, HttpStatusException {
////        return contentLengthValidator.validateContentOnRead(internalRequest.headers().getContentLength(), contentLength ->
////            inputBuffer.readAllBody(contentLength, sizeMonitor.getBodyMonitor())
////        );
//        return new byte[0];
//    }
//
//    @Override
//    public String getAllBodyAsString() throws IOException {
//        return "";
//    }
//
//    // 지연 읽기가 아직 커널 메모리에 저장되기 때문에 애플리케이션 메모리 과부하를 줄읽수 있다. -> parts가 비었으면 처음으로 파싱을 수행
//    @Override
//    public Part getPart(String partName) throws IOException, HttpStatusException {
//        return request.getPart(partName);
//
//
////        ResourceType resourceType = ResourceType.MULTIPART;
////        String contentType = request.headers().getContentType();
////        String boundary = resourceType.getContentTypeParamValue(contentType);
////        if (shouldProcessMultipart(resourceType, contentType)) {
////            contentLengthValidator.validateContentOnRead(request.headers().getContentLength(), contentLength -> {
////                multipartProcessor.processParts(
////                        inputBuffer,
////                        boundary,
////                        request
////                    );
////                    return null;
////                }
////            );
////        }
////        return request.multipart().getPart(partName);
//    }
//
//    @Override
//    public InputStream getInputStream() throws IOException {
//        return request.getInputStream();
//    }
//
//    @Override
//    public String getHeader(String key) {
//        return request.getHeader(key);
//    }
//
//    @Override
//    public String getCookie() {
//        return "";
//    }
//
//    @Override
//    public String getContentType() {
//        return request.getContentType();
//    }
//
//    @Override
//    public int getContentLength() {
//        return request.getContentLength();
//    }
//
//    @Override
//    public String getConnection() {
//        return request.getHeader("connection");
//    }
//
//    @Override
//    public HttpMethod getMethod() {
//        return HttpMethod.fromString(request.getMethod());
//    }
//
//    @Override
//    public String getRequestURI() {
//        return request.getRequestURI();
//    }
//
//    @Override
//    public String getPath() {
//        return request.getPath();
//    }
//
//    @Override
//    public String getQueryString() {
//        return request.getQueryString();
//    }
//
//    @Override
//    public HttpVersion getHttpVersion() {
//        return HttpVersion.fromString(request.getProtocol());
//    }
//
//    @Override
//    public String getRequestParameterValue(String key) throws IOException {
//        return request.getParameter(key);
//    }
//
////    private boolean shouldProcessMultipart(ResourceType resourceType, String contentType) {
////        return !internalRequest.multipart().isLoad() && resourceType.isEqualMimeType(contentType);
////    }
//
////    @Override
////    public BufferedSocketInputStream getInputStream() throws IOException { return request.getInputStream(); }
//
//}
