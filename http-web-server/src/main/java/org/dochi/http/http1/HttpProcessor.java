package org.dochi.http.http1;

import org.dochi.http.api.HttpApiHandler;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.request.HttpVersion;
import org.dochi.http.request.RequestHeaders;
import org.dochi.http.response.HttpResponse;
import org.dochi.http.response.HttpStatus;
import org.dochi.http.response.ResponseHeaders;
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
        int requestCount = 0;
        try  {
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
        int maxKeepAliveRequests = socketWrapper.getKeepAlive().getMaxKeepAliveRequests();
        int requestCount = 0;

        while (requestCount < maxKeepAliveRequests) {
            if (!processRequest(socketWrapper, requestMapper)) {
                break;
            }
            requestCount++;
        }

        log.debug("Total requests processed: {}", requestCount);
    }

    // 예외가 발생하면 JVM은 호출된 메서드들의 스택을 추적하여 예외 발생 지점인 Top부터 호출된 메서드들을 아래로 따라가 예외를 처리할수 있는 메서드까지 에외를 전파시킨다.
    // 계속 스택을 타고 내려가다 bottom에 도착했는데도 예외가 처리되지 않으면 최종적으로 JVM이 예외를 처리하여 스레드는 비정상 종료된다.
    // 스레드가 비정상 종료되어도 try-with-resource나 finally을 사용하면 자원을 닫을수 있다.
    private boolean processRequest(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        try {
            if (!request.prepareHttpRequest()) {
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

    private boolean shouldKeepAlive(SocketWrapper socketWrapper) {
        if (isKeepAlive()) {
            response.addHeader(ResponseHeaders.CONNECTION, "Keep-Alive");
            response.addHeader(ResponseHeaders.KEEP_ALIVE, String.format("timeout=%d, max=%d", socketWrapper.getKeepAlive().getKeepAliveTimeout()/1000, socketWrapper.getKeepAlive().getMaxKeepAliveRequests()));
            return true;
        }
        response.addHeader(ResponseHeaders.CONNECTION, "close");
        return false;
    }

    private void handleRequestException(Exception e) {
        if (e instanceof SocketTimeoutException) {
            // Socket의 soSetTimeout()으로 입력 시간 설정 이후, SocketInputStream 객체의 read() 메서드 의해 blocking 중인 상태에서 유효 시간이 만료되면 SocketTimeoutException 예외 던짐
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답 (write()는 setSoTimeout와 관련 없음)
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
            log.warn("Socket read timeout occurred: {}", e.getMessage());
            handleTimeoutException();
        } else if (e instanceof SocketException) {
            // 클라이언트가 연결 끊었을 때, read() SocketException("Connection reset") 던짐 내부적으로 ConnectionReset 예외 발생
            // 클라이언트가 연결 끊었을 때, write() SocketException("Socket closed") 던짐
            log.warn("Socket was read or write after the client closed connection: {}", e.getMessage());
        } else if (e instanceof IllegalArgumentException) {
            log.warn("Request line has invalid argument: {}", e.getMessage());
            handleInvalidRequestLine();
        } else {
            log.error("Unhandled I/O Exception: {}", e.getMessage());
            handleUnhandledException();
        }
    }

    private void handleTimeoutException() {
        try {
            response.sendError(HttpStatus.REQUEST_TIMEOUT);
        } catch (IOException e) {
            log.error("Failed response keep-alive timeout: {}", e.getMessage());
        }
    }

    private void handleUnhandledException() {
        try {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Failed response internal server error: {}", e.getMessage());
        }
    }

    private void handleInvalidRequestLine() {
        try {
            response.sendError(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Failed response invalid request line: {}", e.getMessage());
        }
    }

    private boolean isKeepAlive() {
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1) && !request.getHeader(RequestHeaders.CONNECTION).equalsIgnoreCase("close")) {
            return true;
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && request.getHeader(RequestHeaders.CONNECTION).equalsIgnoreCase("keep-alive");
    }
}
