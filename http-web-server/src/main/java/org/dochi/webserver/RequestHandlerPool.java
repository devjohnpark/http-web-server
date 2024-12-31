package org.dochi.webserver;

import org.dochi.webserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestHandlerPool {
    private static final Logger log = LoggerFactory.getLogger(RequestHandlerPool.class);
    private final LinkedBlockingQueue<RequestHandler> requestHandlerQueue;
    private final RequestMapper requestMapper;

    public RequestHandlerPool(ServerConfig serverConfig, RequestMapper requestMapper) {
        this.requestHandlerQueue = new LinkedBlockingQueue<>();
        this.requestMapper = requestMapper;
        this.initPool(serverConfig, requestMapper);
    }

    private void initPool(ServerConfig serverConfig, RequestMapper requestMapper) {
        int poolSize = serverConfig.getThreadPool().getMinSpareThreads();
        for (int i = 0; i < poolSize; i++) {
            requestHandlerQueue.offer(new RequestHandler(new SocketWrapper(serverConfig.getKeepAlive()), requestMapper)); // 큐의 끝에 삽입
        }
    }

    public RequestHandler getAvailableRequestHandler(ServerConfig serverConfig) {
        RequestHandler requestHandler = requestHandlerQueue.poll();
        if (requestHandler == null || !requestHandler.getSocketWrapper().isReusable()) {
            requestHandler = new RequestHandler(new SocketWrapper(serverConfig.getKeepAlive()), requestMapper);
        }
        return requestHandler;
    }

    public void recycleRequestHandler(RequestHandler requestHandler) {
        requestHandlerQueue.offer(requestHandler);
    }
}
