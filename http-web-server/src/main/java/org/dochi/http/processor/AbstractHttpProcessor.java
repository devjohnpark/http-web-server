package org.dochi.http.processor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.processor.HttpRequestProcessor;
import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.http.response.HttpStatus;
import org.dochi.webserver.socket.SocketState;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.dochi.webserver.socket.SocketState.*;


public abstract class AbstractHttpProcessor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
    protected final HttpRequestProcessor request;
    protected final Http11ResponseProcessor response;

    protected AbstractHttpProcessor(HttpRequestProcessor request, Http11ResponseProcessor response) {
        this.request = request;
        this.response = response;
    }

//     SocketWrapper - read(Buffer), write(Buffer), close()
//     SocketState.CLOSE를 반환받으면, process()을 호출한 객체에서 SocketWrapper.close() 호출
//     SocketState를 반환하는 process 메서드로 수정
//     SocketState를 반환받은 메서드에서 close나 upgrade 로직을 실행하도록 아키텍쳐 짠다.
//     스레드간에 재활용되는 인스턴스 변수중 중요 변수는 volatile 로 선언
//     HttpProcessor를 재활용하는 아키텍쳐로 수정
//     HttpApiHandler를 저장하는 Container 구현
//     추후, Endpoint, Acceptor, Connector, Protocol 관련 아키첵쳐 수정
    @Override
    public SocketState process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) {
        SocketState state = CLOSED;
        try {
            socketWrapper.startConnectionTimeout(socketWrapper.getKeepAliveTimeout());
            state = service(socketWrapper, httpApiMapper);
        } catch (SocketException e) {
            log.error("Call setSoTimeout() but socket is already closed: {}", e.getMessage());
            sendError(HttpStatus.BAD_REQUEST, "socket is already closed");
        }
        return state;
    }

    protected abstract SocketState service(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper);


    // 헤더 형식이 올바르다. -> 정상 응답 (shouldContinue return true)
    // 헤더 형식이 올바르지 않다. -> throw HttpStatusException() -> response 4XX
    // 입력 소켓이 메세지 첫줄에 EOF 상태 or IOException -> 응답하지 않고 리소스 정리 (refreshResource() or safeRefreshResource() 호출

    protected abstract boolean shouldKeepAlive(SocketWrapper socketWrapper);

    // 요청과 응답 리소스 정리
    protected void recycle() throws IOException {
        request.recycle();
        response.recycle();
    }

    // try 구문에서 예외가 발생했을때 리소스 정리
    protected void safeRecycle() {
        try {
            recycle();
        } catch (IOException e) {
            processException(e);
        }
    }

    // RuntimeException 처리하는 선택권이 개발자에게 있기 때문에, catch를 하지 않아 전파된 RuntimeException은 클라이언트의 잘못된 요청이라 간주하고 400 응답
    // 입출력시 예기치 못한 IOException, Exception은 서버의 문제이므로 500 응답
    protected void processException(Exception e) {
        switch (e) {
            // SocketTimeoutException시, 연결 종료 -> 연결 끊은 이후, 클라리언트가 요청을 보내면 SocketException 발생
            case SocketTimeoutException socketTimeoutException -> {
                // Socket의 soSetTimeout()으로 입력 시간 설정 이후, SocketInputStream 객체의 read() 메서드 의해 blocking 중인 상태에서 유효 시간이 만료되면 SocketTimeoutException 예외 던짐
                // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답 (write()는 setSoTimeout와 관련 없음)
                // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
                // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
                ;

                log.error("Socket read timeout occurred: {}", e.getMessage());
            }
            case SocketException socketException -> {
                // NioSocketImpl.implRead() 메서드에 기재
                // 클라이언트가 연결 끊은 이후, read() 호출시 SocketException("Connection reset") 던짐 내부적으로 ConnectionReset 예외 발생
                // 클라이언트가 연결 끊은 이후, write() 호출시 SocketException("Socket closed") 던짐
//                log.error("Socket was read or write after the client closed connection: {}", e.getMessage());
                log.error("Socket was read or write after the client closed connection: ", e);
            }
            case HttpStatusException httpStatusException -> {
                sendError(httpStatusException.getHttpStatus(), e.getMessage());
            }
            case RuntimeException runtimeException -> {
                sendError(HttpStatus.BAD_REQUEST, e.getMessage());
            }
            case null, default -> { // IOException, Exception
                assert e != null;
                sendError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    private void sendError(HttpStatus status, String errorMessage) {
        log.error("HTTP status: {} {}, Reason: {}", String.valueOf(status.getCode()), status.getMessage(), errorMessage);
        try {
            if (status.getCode() >= 500) {
                response.sendError(status, status.getMessage());
            } else if (status.getCode() >= 400) {
                response.sendError(status, errorMessage);
            }
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}
