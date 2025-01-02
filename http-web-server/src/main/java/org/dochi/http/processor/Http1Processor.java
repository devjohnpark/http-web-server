package org.dochi.http.processor;

import org.dochi.http.api.HttpApiHandler;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.request.HttpVersion;
import org.dochi.http.response.HttpResponse;
import org.dochi.http.response.HttpStatus;
import org.dochi.webserver.RequestMapper;
import org.dochi.webserver.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Http1Processor implements HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http1Processor.class);
    private final HttpRequest request;
    private final HttpResponse response;

    public Http1Processor(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public void process(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        try {
            configureKeepAliveTimeout(socketWrapper);
            processRequests(socketWrapper, requestMapper);
        } catch (SocketException e) {
            log.error("setSoTimeout but socket is closed: {}", e.getMessage());
        }
    }

    private void configureKeepAliveTimeout(SocketWrapper socketWrapper) throws SocketException {
        socketWrapper.getSocket().setSoTimeout(socketWrapper.getKeepAliveTimeout());
    }

    private void processRequests(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        int requestCount = 0;
        int keepAliveTimeout = socketWrapper.getKeepAliveTimeout();
        int maxKeepAliveRequests = socketWrapper.getMaxKeepAliveRequests();
        while (requestCount < maxKeepAliveRequests) {
            if (!processRequest(keepAliveTimeout, maxKeepAliveRequests, requestMapper)) {
                break;
            }
            request.refresh();
            response.refresh();
            requestCount++;
        }
        log.debug("Total requests processed: {}", requestCount);
    }

    private boolean processRequest(int keepAliveTimeout, int maxKeepAliveRequests, RequestMapper requestMapper) {
        try {
            if (!request.prepareRequest()) {
                return false;
            }
            boolean isKeepAlive = shouldKeepAlive(keepAliveTimeout, maxKeepAliveRequests);
            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
            httpApiHandler.handleApi(request, response);
            return isKeepAlive;
        } catch (Exception e) {
            processException(e);
        }
        return false;
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

    private void processException(Exception e) {
        if (e instanceof SocketTimeoutException) {
            // Socket의 soSetTimeout()으로 입력 시간 설정 이후, SocketInputStream 객체의 read() 메서드 의해 blocking 중인 상태에서 유효 시간이 만료되면 SocketTimeoutException 예외 던짐
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답 (write()는 setSoTimeout와 관련 없음)
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
            log.warn("Socket read timeout occurred: {}", e.getMessage());
            sendError(HttpStatus.REQUEST_TIMEOUT);
        } else if (e instanceof SocketException) {
            // 클라이언트가 연결 끊은 이후 read() 호출시 SocketException("Connection reset") 던짐 내부적으로 ConnectionReset 예외 발생
            // 클라이언트가 연결 끊은 이후, write() 호출시 SocketException("Socket closed") 던짐
            log.warn("Socket was read or write after the client closed connection: {}", e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            log.warn("Request line has invalid argument: {}", e.getMessage());
            sendError(HttpStatus.BAD_REQUEST);
        } else {
            log.error("Unhandled I/O Exception: {}", e.getMessage());
            sendError(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(HttpStatus status) {
        try {
            response.sendError(status);
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
}

