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
//    protected final RequestHeaders headers = new RequestHeaders();
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
        request.getParameters().addRequestParameters(request.getMetadata().getQueryString());
        if (ResourceType.URL.isEqualMimeType(request.getHeaders().getContentType())) {
            request.getParameters().addRequestParameters(getAllBodyAsString());
        }
    }

    public String getAllBodyAsString() throws IOException, HttpStatusException {
        return new String(getAllBody(), StandardCharsets.UTF_8);
    }

    public void refresh() throws IOException {
        request.clear();
//        multipartProcessor.clear();
        sizeMonitor.clear();
    }

    public Request getParsedRequest() { return request; }

//    public BufferedReader getReader() throws IOException {
//        return new BufferedReader(new InputStreamReader(
//                new BufferedRequestStream(HttpBufferedInputStream requestStream) {
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
//            public BufferedRequestStream(HttpBufferedInputStream requestStream) {
//                super(new InputStreamReader(requestStream.getInputStream(), StandardCharsets.UTF_8));
////                count = requestStream.copyBuffer(cb);
//
//            }
//        }
//        return new BufferedRequestStream(requestStream);
//    }


//    public BufferedInputStream getInputStream() {
//        class BufferedRequestStream extends BufferedInputStream {
//            public BufferedRequestStream(HttpBufferedInputStream requestStream) {
//                super(requestStream.getInputStream());
//                count = requestStream.copyBuffer(buf);
//            }
//        }
//        return new BufferedRequestStream(requestStream);
//    }

//    public InputStream getInputStream() { return requestStream.getRequestStream(); }

    public HttpMethod getMethod() { return request.getMetadata().getMethod(); }

    public String getPath() { return request.getMetadata().getPath(); }

    public HttpVersion getHttpVersion() { return request.getMetadata().getHttpVersion(); }

    public String getQueryString() { return request.getMetadata().getQueryString(); }

    public String getRequestURI() { return request.getMetadata().getRequestURI(); }

    public String getRequestParameterValue(String key) { return request.getParameters().getRequestParameterValue(key); }

    public String getHeader(String key) { return request.getHeaders().getHeader(key); }

    public String getCookie() { return request.getHeaders().getCookie(); }

    public String getContentType() { return request.getHeaders().getContentType(); }

    public int getContentLength() { return request.getHeaders().getContentLength(); }

    public String getConnection() { return request.getHeaders().getConnection(); }
}
