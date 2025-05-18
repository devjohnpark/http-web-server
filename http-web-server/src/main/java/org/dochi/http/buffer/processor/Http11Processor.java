package org.dochi.http.buffer.processor;

import org.dochi.http.buffer.api.HttpApiMapper;
import org.dochi.inputbuffer.socket.SocketWrapperBase;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.socket.SocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.dochi.webserver.socket.SocketState.*;

public class Http11Processor extends AbstractHttpProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    public Http11Processor(HttpConfig config) {
        super(new Http11RequestProcessor(config.getHttpReqConfig()), new Http11ResponseProcessor(config.getHttpResConfig()));
    }

    public boolean shouldPersistentConnection(SocketWrapperBase<?> socketWrapper) {
        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
    }

    private boolean shouldNext(SocketWrapperBase<?> socketWrapper) {
        boolean isKeepAlive = shouldPersistentConnection(socketWrapper);
        response.addConnection(isKeepAlive);
        if (isKeepAlive) {
            response.addKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests());
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
        String connectionValue = this.request.getHeader("connection");
        if (this.request.getProtocol().equals("HTTP/1.1")) {
            return !(connectionValue != null && connectionValue.equals("close"));
        }
        return this.request.getProtocol().equals("HTTP/1.0") && (connectionValue != null && connectionValue.equals("keep-alive"));
    }

    protected SocketState service(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper) throws IOException {
        SocketState state = OPEN;
        int count = 0;
        while (state == OPEN) {
            if (!request.isProcessHeader()) {
                request.recycle();
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
            httpApiMapper.getHttpApiHandler(request.getPath()).service(request, response);
            response.flush();
            // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
            // flush() has system call cost, it needs to remove inefficient action.
            // 1. Rapping flush method by custom OutputStream.
            // 2. The custom OutputStream declares boolean-isFlushed variable.
            // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
            recycle();
            count++;
        }
        log.debug("Process count: " + count);
        return state;
    }

    private boolean isUpgradeRequest(SocketWrapperBase<?> socketWrapper) {
        return request.getHeader("upgrade") != null;
    }


//    private void sendUpgrade() {
//
//    }
}

