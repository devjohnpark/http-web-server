package org.dochi.connector;

import org.dochi.buffer.MediaType;
import org.dochi.internal.InternalInputStream;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
public class Request implements HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(Request.class);
    private final Connector connector;
    private org.dochi.internal.Request request;
    private InternalInputStream internalInputStream;
    private boolean parametersParsed = false;

    // 각 AbstractProcessor 자식 객체 마다 Adapter 객체를 주입 (Adapter instance created per client)
    // Adapter에서 Connector.createRequest() 호출해서 connector.Request 생성
    // HttpXXProcesoor.service(internal.Request/Response)
    // -> Adapter.service(internal.Request/Response)
    // -> internal.Request를 setRequest 메서드를 통해 주입하고 Adapter 객체의 필드에 connector.Request 저장 (connector.Request == null이면, 생성)

    // connector는 was와 개발자간의 연결하는 역할을 하는 패키지
    // 개발자을 위한 공통 요청 처리 객체이므로, Connector를 주입받아서 Request 생성
    public Request(Connector connector) {
        this.connector = connector;
    }

    // InternalInputStream은 connector.Requsest에서만 쓰므로 해당 객체에서 인스턴스화를 하는 것이 맞다.
    // InputBuffer는 internal.Request와 connector.Request에서 둘다 쓰므로 internal.Request을 통해 InputBuffer를 가져와서 InternalInputStream를 생성한다.
    public void setRequest(org.dochi.internal.Request request) {
        this.request = request;
        this.internalInputStream = new InternalInputStream(request.getInputBuffer());
    }

    // Adapter에서 service를 호출해서 http api handler를 처리한 후에 recycle() 호출
    public void recycle() {
        // low layer
        this.request.recycle();
        this.internalInputStream.recycle();

        // field
        this.parametersParsed = false;
    }

    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        // MultipartStream 생성: MultipartStream(InputStream in, int headerMaxSize, int bodyMaxSize, int fileMaxSize) -> MultipartStream(this.internalBufferedInputStream, this.connector.getMaxPostSize())
        // MultipartStream.readHeaders()
        // MultipartStream.readBody()

        // MultipartParser.parse(MultipartStream multiaprtStream)
        // parse 메서드는 MultipartStream.readHeaders()와 MultipartStream.readBody() 메서드를 통해 멀티 파트 파싱후 멀티 파트 데이터를 가져올수 있는 객체로 저장
        return null;
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
        return request.path().toString();
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

    @Override
    public String getCookie() {
        return "";
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
        return internalInputStream;
    }

    private void parseParameters() throws IOException {
        // 1. 기본 request-uri 파싱
        parseHeaderRequestParameters();
        // 2. 나머지 content-type에 따라 파싱 (multipart/form-data와 application/x-www-form-urlencoded 기본 파싱)
        MediaType mediaType = MediaType.parseMediaType(this.getContentType()); // type/subtype 없으면 null 반환
        if (mediaType == null) {
            return;
        }
        if ("application/x-www-form-urlencoded".equalsIgnoreCase(mediaType.getTypeSubType())) {
            parseBodyRequestParameters();
        } else if ("multipart/form-data".equalsIgnoreCase(mediaType.getTypeSubType())) {
            // getPart() 메서드 주석에서 로직에서 확인
        }
        this.parametersParsed = true;
    }

    private void parseBodyRequestParameters() throws IOException {
        int contentLength = this.getContentLength();
        byte[] buf = new byte[contentLength]; // content-length가 과연 byte 단위인지
        int n = internalInputStream.read(buf, 0, contentLength);
        request.parameters().addRequestParameters(new String(buf, request.getCharsetFromContentType()));
    }

    private void parseHeaderRequestParameters() {
        if (!request.queryString().isNull()) {
            // header와 body의 request parameter 중복시, body 값으로 덮어씌움
            request.parameters().addRequestParameters(this.getQueryString());
        }
    }
}
