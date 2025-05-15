package org.dochi.http.buffer.processor;

import org.dochi.inputbuffer.internal.http11.Http11InputBuffer;
import org.dochi.inputbuffer.socket.SocketWrapperBase;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// internal.Request를 참조
// HttpRequestProcessor 인터페이스를 구현
// Http11RequestProcessor가 connector.Request 역할과 Processor의 역할을 하도록한다.
// 프로토콜 버전에 맞는 InputBuffer로 파싱
public class Http11RequestProcessor extends AbstractHttpRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11RequestProcessor.class);

    private final Http11InputBuffer inputBuffer;

    public Http11RequestProcessor(HttpReqConfig httpReqConfig) {
        super(httpReqConfig);
        this.inputBuffer = new Http11InputBuffer(request, httpReqConfig.getRequestHeaderMaxSize());
    }

    @Override
    public void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        this.inputBuffer.init(socketWrapper);
        this.request.setInputBuffer(inputBuffer);
    }
    public boolean isProcessHeader() throws IOException {
        return inputBuffer.parseHeader();
    }

    // Adapter에서 service를 호출해서 http api handler를 처리한 후에 recycle() 호출
    public void recycle() {
        super.recycle();
        inputBuffer.recycle();
    }
}
