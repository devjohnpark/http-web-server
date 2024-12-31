package org.dochi.http.http1;

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

public class HttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);
    private final HttpRequest request;
    private final HttpResponse response;

    public HttpProcessor(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public void process(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        try {
            configureSocketTimeout(socketWrapper);
            processRequests(socketWrapper, requestMapper);
        } catch (SocketException e) {
            log.error("setSoTimeout but socket is closed: {}", e.getMessage());
        }
    }

    private void configureSocketTimeout(SocketWrapper socketWrapper) throws SocketException {
        int keepAliveTimeout = socketWrapper.getKeepAlive().getKeepAliveTimeout();
        socketWrapper.getSocket().setSoTimeout(keepAliveTimeout);
    }

    private void processRequests(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        int maxKeepAliveRequests = getMaxKeepAliveRequests(socketWrapper);
        int requestCount = 0;

        while (requestCount < maxKeepAliveRequests) {
            if (!processRequest(socketWrapper, requestMapper)) {
                break;
            }
            request.refresh();
            response.refresh();
            requestCount++;
        }

        log.debug("Total requests processed: {}", requestCount);
    }

    private boolean processRequest(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        try {
            if (!request.prepareRequest()) {
                return false;
            }
            boolean moreRequestsPossible = shouldKeepAlive(socketWrapper);
            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
            httpApiHandler.handleApi(request, response);
            return moreRequestsPossible;
        } catch (Exception e) {
            handleRequestException(e);
        }
        return false;
    }

    private void handleRequestException(Exception e) {
        if (e instanceof SocketTimeoutException) {
            // Socket의 soSetTimeout()으로 입력 시간 설정 이후, SocketInputStream 객체의 read() 메서드 의해 blocking 중인 상태에서 유효 시간이 만료되면 SocketTimeoutException 예외 던짐
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답 (write()는 setSoTimeout와 관련 없음)
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
            log.warn("Socket read timeout occurred: {}", e.getMessage());
            sendError(response, HttpStatus.REQUEST_TIMEOUT);
        } else if (e instanceof SocketException) {
            // 클라이언트가 연결 끊은 이후 read() 호출시 SocketException("Connection reset") 던짐 내부적으로 ConnectionReset 예외 발생
            // 클라이언트가 연결 끊은 이후, write() 호출시 SocketException("Socket closed") 던짐
            log.warn("Socket was read or write after the client closed connection: {}", e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            log.warn("Request line has invalid argument: {}", e.getMessage());
            sendError(response, HttpStatus.BAD_REQUEST);
        } else {
            log.error("Unhandled I/O Exception: {}", e.getMessage());
            sendError(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendError(HttpResponse response, HttpStatus status) {
        try {
            response.sendError(status);
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
    private int getMaxKeepAliveRequests(SocketWrapper socketWrapper) {
        return socketWrapper.getKeepAlive().getMaxKeepAliveRequests();
    }

    private boolean shouldKeepAlive(SocketWrapper socketWrapper) {
        boolean isKeepAlive = isKeepAlive();
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
            response.addKeepAlive(socketWrapper.getKeepAlive().getKeepAliveTimeout(), socketWrapper.getKeepAlive().getMaxKeepAliveRequests());
        }
        return isKeepAlive;
    }

    private boolean isKeepAlive() {
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1) && !request.getConnection().equalsIgnoreCase("close")) {
            return true;
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && request.getConnection().equalsIgnoreCase("keep-alive");
    }
}
