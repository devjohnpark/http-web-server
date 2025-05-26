package org.dochi.internal.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.internal.http11.Http11InputBuffer;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.dochi.webserver.socket.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Http11InputBuffer inputBuffer;

    public Http11Processor(HttpConfig config) {
        super(config);
        this.inputBuffer = new Http11InputBuffer(config.getHttpReqConfig().getRequestHeaderMaxSize());
        this.requestHandler.setInputBuffer(this.inputBuffer);
    }

    protected boolean shouldPersistentConnection(SocketWrapperBase<?> socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldNext(SocketWrapperBase<?> socketWrapper) {
        boolean isKeepAlive = shouldPersistentConnection(socketWrapper);
        responseHandler.addConnection(isKeepAlive);
        if (isKeepAlive) {
            responseHandler.addKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests());
        }
        return isKeepAlive;
    }

    private boolean isSeverKeepAlive(SocketWrapperBase<?> socketWrapper) {
        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getMaxKeepAliveRequests());
    }

    private static boolean isReachedMax(int currentCount, int maxCount) {
        return currentCount >= maxCount;
    }

    private boolean isRequestKeepAlive() {
        String connectionValue = this.requestHandler.getHeader("connection");
        if (this.requestHandler.getProtocol().equals("HTTP/1.1")) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return this.requestHandler.getProtocol().equals("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    @Override
    protected void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        inputBuffer.init(socketWrapper);
        // temporary -> outputBuffer.init(socketWrapper);
        responseHandler.init(socketWrapper);
    }

    protected SocketState service(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper) throws IOException {
        SocketState state = OPEN;
        int count = 0;
        while (state == OPEN) {
            if (!inputBuffer.parseHeader(requestHandler.getRequest())) {
                inputBuffer.recycle();
                requestHandler.recycle();
                state = CLOSED;
                break;
            } else if (isUpgradeRequest(socketWrapper)) {
                // Current ignore HTTP/1.1 upgrade request, processing as HTTP/1.1 (Later support HTTP/2.0)
                state = UPGRADING;
                // 1. upgradeToken(); // upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);
                // 2. sendUpgrade(); // HTTP/1.1 response 101 status
                // 3. break;
                // After client preface request -> response as HTTP/2.0 using Http2Processor
            } else if (!shouldNext(socketWrapper)) {
                state = CLOSED;
            }
            httpApiMapper.getHttpApiHandler(requestHandler.getPath()).service(requestHandler, responseHandler);
            responseHandler.flush();
            // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
            // flush() has system call cost, it needs to remove inefficient action.
            // 1. Rapping flush method by custom OutputStream.
            // 2. The custom OutputStream declares boolean-isFlushed variable.
            // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
            recycle(); // 단독으로 inputBuffer.recycle 필요
            count++;
        }
        log.debug("Process count: " + count);
        return state;
    }

    @Override
    protected void recycle() {
        inputBuffer.recycle();
        requestHandler.recycle();
        // outputBuffer.recycle();
        responseHandler.recycle();
    }

    private boolean isUpgradeRequest(SocketWrapperBase<?> socketWrapper) {
        return requestHandler.getHeader("upgrade") != null;
    }


//    private void sendUpgrade() {
//
//    }
}

