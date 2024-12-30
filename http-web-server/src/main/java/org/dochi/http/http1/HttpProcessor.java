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

    private boolean shouldKeepAlive(SocketWrapper socketWrapper) {
        boolean isKeepAlive = isKeepAlive();
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
            response.addKeepAlive(socketWrapper.getKeepAlive().getKeepAliveTimeout(), socketWrapper.getKeepAlive().getMaxKeepAliveRequests());
        }
        return isKeepAlive;
    }

    private void handleRequestException(Exception e) {
        if (e instanceof SocketTimeoutException) {
            log.warn("Socket read timeout occurred: {}", e.getMessage());
            handleTimeoutException();
        } else if (e instanceof SocketException) {
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
        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1) && !request.getConnection().equalsIgnoreCase("close")) {
            return true;
        }
        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && request.getConnection().equalsIgnoreCase("keep-alive");
    }
}
