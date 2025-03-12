package org.dochi.http.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.data.RequestHeaders;
import org.dochi.http.request.processor.Http11RequestProcessor;
import org.dochi.http.request.data.HttpVersion;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    public Http11Processor(InputStream in, OutputStream out, HttpConfig config) {
        super(
                new Http11RequestProcessor(new Http11RequestStream(in), config.getHttpReqConfig()),
                new Http11ResponseProcessor(out, config.getHttpResConfig())
        );
    }

    // keep-alive timeout over -> socketTimeoutException -> send Connection: close 헤더 추가해서 응답하지 못함
    // keep-alive count >= max keep-alive count -> send Connection: close 헤더 추가해서 응답

    // keep-alive max 값에 도달했지만, close 헤더 필드 값을 설정하지않고 서버에서 소켓을 닫아서, TCP 스택에서 socket read error 발생

    // 1. Client -> GET -> Server
    // 2. Server -> FIN, ACK -> Client 서버에서 클라에게 얘기도 없이 소켓 연결을 끊는다고 알림
    // 3. Server -> RST, ASK -> Client
    // 4. Client -> ASK -> Server
    // 5. Server -> RST -> Client
    // 6. Client -> TCP 3 way handshake -> Server

    // 1. 서버에서 클라에게 얘기도 없이(Connect:close 헤더 설정), 소켓 close()를 호출해서 FIN, ACK 패킷 전송
    // 2. 클라에서 FIN, ACK 패킷을 받기전, GET 요청을 함
    // 3. 서버는 GET 요청을 받아서 연결을 강제 종료하였다고 RST, ACK 패킷을 전송 (정상적인 TCP 4-way handshake로 정상 종료한게 아님)
    // 4. 클라는 FIN, ACK 패킷을 받아서 알겠다고 ACK 패킷을 서버에게 전송
    // 5. 서버에서 ACK 패킷을 받아서 연결을 강제 종료했다고 RST 패킷을 클라에게 전송
    // 6. 클라는 서버와 다시 TCP 연결 시도


    // The packet arrives at a TCP connection that was previously established, but the local application already closed its socket or exited and the OS closed the socket.
    // The client closes the socket cuz request max count to server, although the server send a packet reach processing request max count.
    // If keep using Connection: keep-alive, the client doesn't close the socket.
    // Server can close the socket anytime, so keep-alive timeout and max value just reference thing.
    // But server should notice will close the socket to the client.

    public boolean shouldKeepAlive(SocketWrapper socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }


    private boolean shouldNext(SocketWrapper socketWrapper) {
        boolean isKeepAlive = shouldKeepAlive(socketWrapper);
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
           response.addKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests());
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(SocketWrapper socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getMaxKeepAliveRequests());
    }

    private static boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

    private boolean isRequestKeepAlive() {
        String connectionValue = request.getConnection();
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1)) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    // UPGRADING 요청이면, HTTP API 실행하지 않고 소켓 상태 반환
    protected SocketState service(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) {
        SocketState state = OPEN;
        int processCount = 0;
        try {
            // 멀티스레딩 환경에서 동시에 객체에 접근안할지라도 공유 자원의 수정이 CPU 캐시에만 반영되고 메인 메모리에 반영되지 않을수 있기때문에
            // 메모리 가시성을 확보하기 위해서는 객체 사용전에 초기화를 하는 것이 volatile 변수를 사용하는 것보다 성능이 좋다.
            // volatile 변수는 hardware level 에서 메모리의 가시성을 위해서 CPU에 수정된 값을 메인 메모리에 즉시 반영한다. 따라서 성능의 오버헤드가 발생한다.
            recycle();
            while (state == OPEN) {
                if (!request.isPrepareHeader()) {
                    request.recycle();
                    state = CLOSED;
                    break;
                }
                if (isUpgradeRequest(socketWrapper)) {
                    // 현재는 서버가 업그레이드 제안을 무시하고 원본 요청에 대해 HTTP/1.1로 처리하도록한다.
                    // 향후 http2 프로토콜로 통신하는 기능을 추가할때 수월하도록 구조만 잡는다.
                    // upgrade token(Upgrade, HTTP2-Settings)만 복제 (Request 전체 복제 하면 낭비)

                    state = UPGRADING;
                    // upgradeToken 업데이트: upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);

                    // 이후, HTTP/1.1로 101 응답 -> preface 요청 -> HTTP/2.0으로 응답
                    // 1. HTTP/1.1로 101 응답을 DefaultHttpApiHandler에 로직 작성
                    // 2. process 메서드 호출 객체가 UPGRADING 상태를 받아서 Http2Processor로 변경
                } else if (!shouldNext(socketWrapper)) {
                    state = CLOSED;
                }
                httpApiMapper.getHttpApiHandler(request.getPath()).service(request, response);
                response.getOutputStream().flush();
                recycle(); // request.recycle(): multipart/form-data 파일을 일시적으로 저장하도록해서 요청 처리 이후 파일 바로 삭제하기 위해 recycle()
                // response.flush();
                // 그런데 멀티파트에서 파일을 꼭 요청 처리 이후에 삭제해야할까?
                // 멀티파트에서 파일은 용량이 크기때문에 가급적 바로 삭제하는 것이 좋다.
                processCount++;
            }
        } catch (Exception e) {
            processException(e);
            safeRecycle();
            state = CLOSED;
        }
        log.debug("Processed keep-alive requests count: {}", processCount);
        return state;
    }

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
    private boolean isUpgradeRequest(SocketWrapper socketWrapper) throws CloneNotSupportedException {
//        if (!request.getHeader(RequestHeaders.UPGRADE).isEmpty()) {
//            새롭게 설정한 로직 (SocketWrapper가 SocketState를 저장하지 않고 메서드로 반환)
//            1. ProtocolHandler가 SocketState.Upgrading을 반환받는다.
//            2. ProtocolHandler가 UpgradeToken getUpgradeToken() 이나 Request getUpgradeRequest() 호출
//            3. ProtocolHandler의 AbstractProcessor createProcessor(http2) 호출하여, Request이나 UpgradeToken를 주입해서 Http2Processor를 생성
//
//            기존의 설정한 로직 (SocketWrapper가 SocketState를 저장)
//            socketWrapper.markUpgrading();
//            Request upgradeRequest = this.request.getParsedRequest().clone();

            // 1. HttpRequestProcessor로부터 파싱된 요청 데이터(request.getParsedRequest())를 가져와서 clone (101 응답보내면 저장된 요청 데이터는 refresh된다.)
            // 2. upgrade 전용 응답 (HTTP/1.1) -> sendUpgrade()
            // 3. Listener나 call back 패턴으로 ProtocolHandler에게 clone한 헤더 객체를 넘긴다. (얕은 복사를 해도 Request 객체는 반복문을 탈출해서 데이터 변환없음)
            // 4. ProtocolHandler는 기존의 사용하던 소켓 입출력스트림으로 Http2Processor 객체를 생성하고 process 메서드를 실행시킨다.
            // 5. clone한 헤더 객체로 원본 응답을 수행한다. (?)
//
//            return true;
//        }
//        return false;
        return  request.getHeader(RequestHeaders.UPGRADE) != null;
    }

//    private void sendUpgrade() {
//        try {
//            response.sendUpgrade();
//        } catch (Exception e) {
//            processException(e);
//        }
//    }
}

