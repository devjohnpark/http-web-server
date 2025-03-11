package org.dochi.http.processor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.request.processor.HttpRequestProcessor;
import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.http.response.HttpStatus;
import org.dochi.webserver.socket.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public abstract class AbstractHttpProcessor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
    protected final HttpRequestProcessor request;
    protected final Http11ResponseProcessor response;

    protected AbstractHttpProcessor(HttpRequestProcessor request, Http11ResponseProcessor response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void process(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) {
        try {
            socketWrapper.startConnectionTimeout(socketWrapper.getKeepAliveTimeout());
            processRequests(socketWrapper, httpApiMapper);
        } catch (SocketException e) {
            log.error("Call setSoTimeout() but socket is already closed: {}", e.getMessage());
            sendError(HttpStatus.BAD_REQUEST, "socket is already closed");
        }
    }

    protected void processRequests(SocketWrapper socketWrapper, HttpApiMapper httpApiMapper) {
        int processCount = 0;
        try {
            while (shouldContinue(socketWrapper)) {
                httpApiMapper.getHttpApiHandler(request.getPath()).service(request, response);
                refreshResource();
                processCount++;
            }
        } catch (Exception e) {
            processException(e);
            safeRefreshResource();
        }
        log.debug("Processed keep-alive requests count: {}", processCount);
    }


    protected boolean shouldContinue(SocketWrapper socketWrapper) throws Exception {
        // 다음 두 조건 중 하나라도 부합하지 않으면 false 반환
        // 1. SocketState.CLOSING -> false -> break
        // 2. !request.isPrepareHeader() -> false -> break
        if (socketWrapper.isClosing() || !request.isPrepareHeader()) {
            return false;
        }
        // 3. keep-alive 조건 안맞으면 다음 요청부터 작업 중자하기 위해서 CLOSING 설정
        if (!shouldNextRequest(socketWrapper)) {
            socketWrapper.markClosing();
        }
        // 4. shouldContinue을 오버라이딩해서 upgrade 연결이면 false -> break;
        return true;
    }

    protected abstract boolean shouldNextRequest(SocketWrapper socketWrapper);


    // 요청과 응답 리소스 정리
    private void refreshResource() throws IOException {
        request.recycle();
        response.recycle();
    }

    // try 구문에서 예외가 발생했을때 리소스 정리
    private void safeRefreshResource() {
        try {
            refreshResource();
        } catch (IOException e) {
            processException(e);
        }
    }

    // RuntimeException 처리하는 선택권이 개발자에게 있기 때문에, catch를 하지 않아 전파된 RuntimeException은 클라이언트의 잘못된 요청이라 간주하고 400 응답
    // 입출력시 예기치 못한 IOException, Exception은 서버의 문제이므로 500 응답
    protected void processException(Exception e) {
        switch (e) {
            case SocketTimeoutException socketTimeoutException -> {
                // Socket의 soSetTimeout()으로 입력 시간 설정 이후, SocketInputStream 객체의 read() 메서드 의해 blocking 중인 상태에서 유효 시간이 만료되면 SocketTimeoutException 예외 던짐
                // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답 (write()는 setSoTimeout와 관련 없음)
                // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
                // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
                sendError(HttpStatus.REQUEST_TIMEOUT, "Socket read timeout occurred.");
            }
            case SocketException socketException ->
                // 클라이언트가 연결 끊은 이후 read() 호출시 SocketException("Connection reset") 던짐 내부적으로 ConnectionReset 예외 발생
                // 클라이언트가 연결 끊은 이후, write() 호출시 SocketException("Socket closed") 던짐
                log.debug("Socket was read or write after the client closed connection: {}", e.getMessage());
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
