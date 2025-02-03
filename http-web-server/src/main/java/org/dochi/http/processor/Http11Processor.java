package org.dochi.http.processor;

import org.dochi.http.request.processor.Http11RequestProcessor;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    public Http11Processor(InputStream in, OutputStream out, HttpConfig config) {
        super(
                new Http11RequestProcessor(new Http11RequestStream(in), config.getHttpReqConfig()),
                new Http11ResponseProcessor(out, config.getHttpResConfig())
        );
    }

    @Override
    protected boolean shouldNextRequest(SocketWrapper socketWrapper) {
        return shouldKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests())
                && socketWrapper.incrementKeepAliveCount() < socketWrapper.getMaxKeepAliveRequests();
    }

    private boolean shouldKeepAlive(int timeout, int maxRequests) {
        boolean isKeepAlive = isKeepAlive();
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
            response.addKeepAlive(timeout, maxRequests);
        }
        return isKeepAlive;
    }

    private boolean isKeepAlive() {
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1)) {
            return !request.getConnection().equalsIgnoreCase("close");
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && request.getConnection().equalsIgnoreCase("keep-alive");
    }

    // http2 업그레이드
    //    @Override
//    protected boolean shouldContinue(SocketWrapper socketWrapper) throws Exception {
//        // shouldNextRequest 메서드에 if (request.getConnection("Upgrade")) -> socketWrapper.setSocketState = UPGRADING -> break;
//        return super.shouldContinue(socketWrapper) && !isUpgradeConnection(socketWrapper);
//    }

    /*
    GET / HTTP/1.1
    Host: localhost:8080
    Connection: Upgrade, HTTP2-Settings
    Upgrade: h2c
    HTTP2-Settings: AAMAAABkAAQAoAAAAAIAAAAA
    */

    // h2c: 비암호화된 HTTP/2의 방식
    // HTTP2-Settings: HTTP/2 설정 정보: 최대 스트림 개수, 초기 윈도우 크기 등

    // h2c(HTTP/2 Cleartext) 업그레이드 과정
    // 1. HTTP/1.1로 Upgrade: h2c 헤더로 HTTP/2.0 업그레이드 제안
    // 2. 서버에서 HTTP/2.0 업그레이드를 수락했다고 101 Switching Protocol 응답
    // 3. 파싱한 요청 데이터(Upgrade, HTTP2-Settings)가 저장된 객체를 리스너나 콜백 패턴으로 넘겨서 HTTP/2.0 프로토콜 설정
    // 4. 101 응답을 준 후 지속 연결 유지한채로 Http2Processor 객체 생성 (연결이 끊기면 무효화되어 HTTP/2.0으로 요청 진행 안함)
    // 5. 서버는 HTTP/2 연결 프리페이스(connection preface) 수신 대기
    // 6. 클라는 101 응답에 대해 서버의 HTTP/2.0 전환을 확인하기 위한 매직 문자열("PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n")과 SETTINGS 프레임 전송
    // 7. 서버도 자신의 SETTINGS 프레임으로
    // 8. 이후 클라이언트와 서버는 HTTP/2 프로토콜을 사용하여 요청

    // 현재는 서버가 업그레이드 제안을 무시하고 원본 요청에 대해 HTTP/1.1로 처리해놓는다.
    // 향후 http2 프로토콜로 통신하는 기능을 추가할때 수월하도록 구조만 잡는다.
//    private boolean isUpgradeConnection(SocketWrapper socketWrapper) throws CloneNotSupportedException {
//        if (!request.getHeader(RequestHeaders.UPGRADE).isEmpty()) {
//            socketWrapper.markUpgrading();
//            Request upgradeRequest = this.request.getParsedRequest().clone();
//
//            // 1. HttpRequestProcessor로부터 파싱된 요청 데이터(request.getParsedRequest())를 가져와서 clone (101 응답보내면 저장된 요청 데이터는 refresh된다.)
//            // 2. upgrade 전용 응답 (HTTP/1.1) -> sendUpgrade()
//            // 3. Listener나 call back 패턴으로 ProtocolHandler에게 clone한 헤더 객체를 넘긴다. (얕은 복사를 해도 Request 객체는 반복문을 탈출해서 데이터 변환없음)
//            // 4. ProtocolHandler는 기존의 사용하던 소켓 입출력스트림으로 Http2Processor 객체를 생성하고 process 메서드를 실행시킨다.
//            // 5. clone한 헤더 객체로 원본 응답을 수행한다. (?)
//
//            return true;
//        }
//        return false;
//    }

//    private void sendUpgrade() {
//        try {
//            response.sendUpgrade();
//        } catch (Exception e) {
//            processException(e);
//        }
//    }
}

