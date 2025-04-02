package org.dochi.buffer.connector;

import org.dochi.buffer.internal.InternalInputStream;
import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.HttpMethod;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;
import java.io.InputStream;

//
public class Request implements HttpRequest {
    private InternalInputStream internalInputStream;
    private org.dochi.buffer.internal.Request request;
//    private final Connector connector;
//
//    // connector.request는 was와 개발자간의 연결 패키지
//    // 개발자을 위한 공통 요청 처리 객체이므로, Connector를 주입받아서 Request 생성
//    // HttpApiAdapter가 Connector를 참조하고 있고 HttpApiAdapter에사 Connector를 주입해서 connector.Request 생성
//    public Request(Connector connector) {
//        this.connector = connector;
//    }

//    private final Connector connector;
//    public Request(Connector connector, org.dochi.buffer.internal.Request request) {
//        this.internalInputStream = new InternalInputStream(request.getInputBuffer());
//    }

    // Adapter에서 Connector.createRequest() 호출해서 connector.Request 생성
    // HttpXXProcesoor.service(internal.Request/Response)
    // -> Adapter.service(internal.Request/Response)
    // -> internal.Request 주입해서 생성 및 Adapter 필드에 저장 (Request == null이면, 생성)
    // -> internalAdapter.service(internal.Request/Response) 호출시 마다, duplicateBuffer() 호출 필요

//    private final Connector connector;
//
//    public Request(Connector connector) {
//        this.connector = connector;
//    }
//
//    public void setRequest(org.dochi.buffer.internal.Request request) {
//        this.request = request;
//        this.internalInputStream = new InternalInputStream(request.getInputBuffer());
//    }

    public Request(org.dochi.buffer.internal.Request request) {
        this.internalInputStream = new InternalInputStream(request.getInputBuffer());
    }

    // HttpProcesoor 마다 하나의 Adapter 생성: new Http11Processor(new Adapter)
    // Adapter.service(internal.Request/Response)
    // -> service 메서드 내에서 connector.Request 생성 및 필드 저장 (필드에 저장된 값 없으면 생성)
    // -> setRequest(internal.Request) 호출해서 참조하도록 한다.
//    public void setRequest(org.dochi.buffer.internal.Request request) {
//        this.request = request;
//    }

    // Adapter에서 service를 호출해서 http api handler를 처리한 후에 recycle() 호출
    public void recycle() {
        this.request.recycle();
        this.internalInputStream.recycle();
    }


//    // HttpXXProcessor에서 프로토콜에 맞는 InputBuffer 주입
//    public void setInputBuffer(InputBuffer inputBuffer) {
//        this.internalInputStream = new InternalInputStream(inputBuffer);
//    }

    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {


        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return internalInputStream;
    }

    @Override
    public String getMethod() {
//        return request.method().toString();
        return "";
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public HttpVersion getHttpVersion() {
        return null;
    }

    @Override
    public String getHeader(String key) {
        return "";
    }

    @Override
    public String getCookie() {
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getConnection() {
        return "";
    }

    @Override
    public String getRequestParameter(String key) {
        return "";
    }

    @Override
    public InternalInputStream getDochiInputStream() throws IOException {
        return internalInputStream;
    }
}
