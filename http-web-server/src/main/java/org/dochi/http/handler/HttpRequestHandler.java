package org.dochi.http.handler;

import org.dochi.external.InputBuffer;
import org.dochi.external.InternalInputStream;
import org.dochi.internal.Request;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.data.multipart.Part;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.data.MediaType;
import org.dochi.http.data.multipart.MultiPartParser;
import org.dochi.http.data.multipart.Multipart;
import org.dochi.http.data.multipart.MultipartStream;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class HttpRequestHandler implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    protected Request request;
    private InternalInputStream inputStream;
    private org.dochi.internal.buffer.InputBuffer internalInputBuffer;
    private final InputBuffer inputBuffer;
    private final Multipart multipart;
    private boolean parametersParsed = false;
    private boolean multipartParsed = false;
    private boolean usingInputStream = false;
    private final HttpReqConfig config;

    public HttpRequestHandler(HttpReqConfig httpReqConfig) {
        this.request = new Request();
        this.inputBuffer = new InputBuffer();
        this.inputStream = new InternalInputStream(this.inputBuffer);
        this.multipart = new Multipart();
        this.config = httpReqConfig;
    }

    public void setInputBuffer(org.dochi.internal.buffer.InputBuffer inputBuffer) {
        if (inputBuffer == null) {
            throw new IllegalArgumentException("internal.InputBuffer is null");
        }
        this.inputBuffer.setInputBuffer(inputBuffer);
    }

    public Request getRequest() {
        return request;
    }

    // Adapter에서 service를 호출해서 http api handler를 처리한 후에 recycle() 호출
    @Override
    public void recycle() {
        this.request.recycle();
        this.inputBuffer.recycle();
        this.multipart.recycle();
        this.parametersParsed = false;
        this.multipartParsed = false;

        if (this.inputStream != null) {
            this.inputStream.clear();
            this.inputStream = null;
        }
    }

    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        if (!this.parametersParsed) {
            parseParameters();
        }
        if (!this.multipartParsed) {
            MultiPartParser parser = new MultiPartParser(new MultipartStream(getInputStream()), config.getRequestHeaderMaxSize(), config.getRequestPayloadMaxSize());
            try {
                parser.parseParts(getParameter("boundary"), multipart);
            } catch (IllegalStateException e) {
                throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            multipartParsed = true;
        }
        return multipart.getPart(partName);
    }

    @Override
    public String getMethod() { return request.method().toString(); }

    @Override
    public String getRequestURI() { return request.requestURI().toString(); }

    @Override
    public String getPath() { return request.requestPath().toString(); }

    @Override
    public String getQueryString() {
        return request.queryString().toString();
    }

    @Override
    public String getProtocol() { return request.protocol().toString(); }

    @Override
    public String getHeader(String key) {
        return request.headers().getHeader(key);
    }

    // 헤더 값은 조회해야되므로 다시 조회할려면 검색 시간이 걸린다. 따라서 필수로 필요한 헤더 값은 internal.Reqeust을 통해 메모리 주소를 다이렉트로 참조해서 반환하도록한다.
    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    // 웹 서버 기본 파싱
    // 1. request-uri에 queryString이 존재한다면 파싱해서 파라매터로 저장
    // 2. 나머지 content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)
    @Override
    public String getParameter(String key) throws IOException {
        // 웹서버 기본 파싱 한번만 수행
        if (!this.parametersParsed) {
            parseParameters();
        }
        return request.parameters().getValue(key);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (usingInputStream) {
            throw new IllegalStateException("HttpRequestHandler.getInputStream already used");
        }
        usingInputStream = true;
        if (this.inputStream == null) {
            this.inputStream = new InternalInputStream(this.inputBuffer);
        }
        return this.inputStream;
    }

    // this.connector.getMaxPostSize()로 제어값 불러온다.
    private void parseParameters() throws IOException {
        // 1. 기본 request-uri 파싱
        parseHeaderRequestParameters();
        // 2. 나머지 content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)
        MediaType mediaType = MediaType.parseMediaType(this.getContentType()); // type/subtype 없으면 null 반환
//        if (mediaType == null) {
//            throw new IllegalArgumentException("Media type is invalid");
//        }
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(mediaType.getFullType())) {
            parseBodyRequestParameters();
        } else if ("multipart/form-data".equalsIgnoreCase(mediaType.getFullType())) {
            // getPart() 메서드 주석에서 로직에서 확인
            request.parameters().addParameter(mediaType.getParameterName(), mediaType.getParameterValue()); // boundary
        }
        this.parametersParsed = true;
    }

    private void parseBodyRequestParameters() throws IOException {
        int contentLength = this.getContentLength();
        byte[] buf = new byte[contentLength];
        int n = 0;
        InputStream in = getInputStream();
        while (n < contentLength) {
            n += in.read(buf, n, contentLength - n);
        }
        request.parameters().addRequestParameters(new String(buf, request.getCharsetFromContentType()));
    }

    private void parseHeaderRequestParameters() {
        if (!request.queryString().isNull()) {
            // header와 body의 request parameter 중복시, body 값으로 덮어씌움
            request.parameters().addRequestParameters(this.getQueryString());
        }
    }
}
