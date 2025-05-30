package org.dochi.http.request.processor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.*;
import org.dochi.http.monitor.HttpMessageSizeManager;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.request.multipart.MultiPartProcessor;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class AbstractHttpRequestProcessor implements HttpRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpRequestProcessor.class);

//    protected final RequestMetadata requestMetadata = new RequestMetadata();
//    protected final MimeHeaders headers = new MimeHeaders();
//    protected final RequestParameters parameters = new RequestParameters();

    // maxFileSize = 10485760,       // 10MB (파일 크기 제한), 무한대 -1 -> BigInt
    // maxRequestSize = 52428800     // 50MB (전체 요청 크기 제한), 무한대 -1 -> BigInt
    // maxPostSize (기본값: 2MB), 무한대 -1 -> BigInt

    protected final Request request;
    protected final MultiPartProcessor multipartProcessor;
    protected final HttpMessageSizeManager sizeMonitor;

    protected AbstractHttpRequestProcessor(HttpReqConfig httpReqConfig) {
        this.request = new Request();
        this.sizeMonitor = new HttpMessageSizeManager(httpReqConfig.getRequestHeaderMaxSize(), httpReqConfig.getRequestBodyMaxSize());
        this.multipartProcessor = new MultiPartProcessor(sizeMonitor.getBodyMonitor());
    }

    public boolean isPrepareHeader() throws IOException, HttpStatusException {
        if (isProcessHeader(sizeMonitor.getHeaderMonitor())) {
            processParameters();
            return true;
        }
        return false;
    }

    protected abstract boolean isProcessHeader(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException;

    protected void processParameters() throws IOException, HttpStatusException {
        request.parameters().addRequestParameters(request.metadata().getQueryString());
        if (ResourceType.URL.isEqualMimeType(request.headers().getContentType())) {
            request.parameters().addRequestParameters(getAllBodyAsString());
        }
    }

    public String getAllBodyAsString() throws IOException, HttpStatusException {
        return new String(getAllBody(), StandardCharsets.UTF_8);
    }

    public void recycle() throws IOException {
        request.clear();
        sizeMonitor.clear();
    }

    public Request getParsedRequest() { return request; }

//    public BufferedReader getReader() throws IOException {
//        return new BufferedReader(new InputStreamReader(
//                new BufferedRequestStream(SocketBufferedInputStream requestStream) {
//                    super(requestStream.getInputStream());
//                    count = requestStream.copyBuffer(buf);
//                }
//                , StandardCharsets.UTF_8));
//    }

//    public BufferedReader getReader() {
//        return new BufferedReader(
//                new InputStreamReader(requestStream.getInputStream(), StandardCharsets.UTF_8),
//                requestStream.copyBuffer()
//        );
//    }


//    public BufferedReader getReader() {
//        class BufferedRequestStream extends BufferedReader {
//            public BufferedRequestStream(SocketBufferedInputStream requestStream) {
//                super(new InputStreamReader(requestStream.getInputStream(), StandardCharsets.UTF_8));
////                count = requestStream.copyBuffer(cb);
//
//            }
//        }
//        return new BufferedRequestStream(requestStream);
//    }


//    public BufferedInputStream getInputStream() {
//        class BufferedRequestStream extends BufferedInputStream {
//            public BufferedRequestStream(SocketBufferedInputStream requestStream) {
//                super(requestStream.getInputStream());
//                count = requestStream.copyBuffer(buf);
//            }
//        }
//        return new BufferedRequestStream(requestStream);
//    }

//    public InputStream getInputStream() { return requestStream.getRequestStream(); }

    public HttpMethod getMethod() { return request.metadata().getMethod(); }

    public String getPath() { return request.metadata().getPath(); }

    public HttpVersion getHttpVersion() { return request.metadata().getHttpVersion(); }

    public String getQueryString() { return request.metadata().getQueryString(); }

    public String getRequestURI() { return request.metadata().getRequestURI(); }

    public String getRequestParameterValue(String key) { return request.parameters().getRequestParameterValue(key); }

    public String getHeader(String key) { return request.headers().getHeader(key); }

    public String getCookie() { return request.headers().getCookie(); }

    public String getContentType() { return request.headers().getContentType(); }

    public int getContentLength() { return request.headers().getContentLength(); }

    public String getConnection() { return request.headers().getConnection(); }
}
