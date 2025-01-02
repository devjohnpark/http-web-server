package org.dochi.webserver;

import org.dochi.webserver.config.KeepAlive;
import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.config.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestHandlerPool {
    private static final Logger log = LoggerFactory.getLogger(RequestHandlerPool.class);
    private final LinkedBlockingQueue<RequestHandler> requestHandlerQueue;

    public RequestHandlerPool(int poolSize, KeepAlive keepAlive, RequestMapper requestMapper) {
        this.requestHandlerQueue = new LinkedBlockingQueue<>();
        this.initPool(poolSize, keepAlive, requestMapper);
    }

    private void initPool(int poolSize, KeepAlive keepAlive, RequestMapper requestMapper) {
        for (int i = 0; i < poolSize; i++) {
            requestHandlerQueue.offer(new RequestHandler(new SocketWrapper(keepAlive.getKeepAliveTimeout(), keepAlive.getMaxKeepAliveRequests()), requestMapper)); // 큐의 끝에 삽입
        }
        log.info("RequestHandlerPool initialized, Total size: {}.", requestHandlerQueue.size());
    }

    public RequestHandler getAvailableRequestHandler(KeepAlive keepAlive, RequestMapper requestMapper) {
        RequestHandler requestHandler = requestHandlerQueue.poll();
        if (requestHandler == null || requestHandler.getSocketWrapper().isUsing()) {
            requestHandler = new RequestHandler(new SocketWrapper(keepAlive.getKeepAliveTimeout(), keepAlive.getMaxKeepAliveRequests()), requestMapper);
        }
        return requestHandler;
    }

    public void recycleRequestHandler(RequestHandler requestHandler) {
        requestHandlerQueue.offer(requestHandler);
    }

    public int getPoolSize() {
        return requestHandlerQueue.size();
    }
}
