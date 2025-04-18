package org.dochi.connector;

import org.dochi.buffer.MediaType;
import org.dochi.http.multipart.MultiPartParser;
import org.dochi.http.multipart.Multipart;
import org.dochi.http.multipart.MultipartStream;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.multipart.Part;
import org.dochi.http.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// Layer에 따른 예외처리 필요
public class Request implements HttpExternalRequest {
    private static final Logger log = LoggerFactory.getLogger(Request.class);
    private final Connector connector;
    private org.dochi.internal.Request request;
    private InternalInputStream inputStream;
    private final InputBuffer inputBuffer;
    private final Multipart multipart;
    private boolean parametersParsed = false;
    private boolean multipartParsed = false;

    // 각 AbstractProcessor 자식 객체 마다 Adapter 객체를 주입 (Adapter instance created per client)
    // Adapter에서 Connector.createRequest() 호출해서 connector.Request 생성
    // HttpXXProcesoor.service(internal.Request/Response)
    // -> Adapter.service(internal.Request/Response)
    // -> internal.Request를 setRequest 메서드를 통해 주입하고 Adapter 객체의 필드에 connector.Request 저장 (connector.Request == null이면, 생성)

    // connector는 was와 개발자간의 연결하는 역할을 하는 패키지
    // 개발자을 위한 공통 요청 처리 객체이므로, Connector를 주입받아서 Request 생성
    public Request(Connector connector) {
        this.connector = connector;
        this.inputBuffer = new InputBuffer();
        this.inputStream = new InternalInputStream(inputBuffer);
        this.multipart = new Multipart();
    }

    // InternalInputStream은 connector.Requsest에서만 쓰므로 해당 객체에서 인스턴스화를 하는 것이 맞다.
    // InputBuffer는 internal.Request와 connector.Request에서 둘다 쓰므로 internal.Request을 통해 InputBuffer를 가져와서 InternalInputStream를 생성한다.

    // 서로 다른 계층인 connector.Request에 internal.Request을 주입할때 생성자가 아닌 메서드로 주입하는 이유: 지연 초기화(lazy init)와 생명주기 분리
    // internal.Request 객체는 프로토콜 별 호환 객체인 InputBuffer 구현체를 참조한다.
    // InputBuffer 구현체는 HTTP2 업그레이드 등 클라이언트와의 프로토콜을 변경할수 있다.
    // 따라서 internal 계층에서 동적으로 InputBuffer 구현체가 변경될수 있다는 얘기이다.
    // 그러므로 setter를 통해 주입해야하며, 프로토콜별 공통 처리를 위해 connector.InputBuffer로 internal.Request를 감싸야한다.
    public void setInternalRequest(org.dochi.internal.Request request) {
        this.request = request;
        this.inputBuffer.setRequest(request);
    }

    // Adapter에서 service를 호출해서 http api handler를 처리한 후에 recycle() 호출
    public void recycle() {
        this.inputBuffer.recycle(); // internal.Request.recycle() 포함
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
            MultiPartParser parser = new MultiPartParser(new MultipartStream(getInputStream()), 1024, 8192);
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
    public String getMethod() {
        return request.method().toString();
    }

    @Override
    public String getRequestURI() {
        return request.requestURI().toString();
    }

    @Override
    public String getPath() {
        return request.requestPath().toString();
    }

    @Override
    public String getQueryString() {
        return request.queryString().toString();
    }

    @Override
    public String getProtocol() {
        return request.protocol().toString();
    }

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
    public InternalInputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            this.inputStream = new InternalInputStream(this.inputBuffer);
        }
        return this.inputStream;
    }

    private void parseParameters() throws IOException {
        // 1. 기본 request-uri 파싱
        parseHeaderRequestParameters();
        // 2. 나머지 content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)
        MediaType mediaType = MediaType.parseMediaType(this.getContentType()); // type/subtype 없으면 null 반환
        if (mediaType == null) {
            return;
        };
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(mediaType.getTypeSubType())) {
            parseBodyRequestParameters();
        } else if ("multipart/form-data".equalsIgnoreCase(mediaType.getTypeSubType())) {
            // getPart() 메서드 주석에서 로직에서 확인
            request.parameters().addParameter(mediaType.getParameterName(), mediaType.getParameterValue()); // boundary
        }
        this.parametersParsed = true;
    }

    private void parseBodyRequestParameters() throws IOException {
        int contentLength = this.getContentLength();
        byte[] buf = new byte[contentLength]; // content-length가 과연 byte 단위인지, 30
//        int n = internalInputStream.read(buf, 0, contentLength);
        int n = 0;
        while (n < contentLength) {
            n += inputStream.read(buf, n, contentLength - n);
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
