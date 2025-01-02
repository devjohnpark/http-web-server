package org.dochi.webserver;

import org.dochi.webserver.config.KeepAlive;
import org.dochi.webserver.config.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerPoolTest {
    private RequestHandlerPool requestHandlerPool;
    private final int poolSize = 10;
    private final KeepAlive keepAlive = new KeepAlive();
    private final RequestMapper requestMapper = new RequestMapper(new ServerConfig().getWebService().getServices());

    @BeforeEach
    void setUp() {
        requestHandlerPool = new RequestHandlerPool(poolSize, keepAlive, requestMapper);
    }

    @Test
    void getAvailableRequestHandler() {
        for (int i = 0; i < poolSize; i++) {
            requestHandlerPool.getAvailableRequestHandler(keepAlive, requestMapper);
        }
        assertNotNull(requestHandlerPool.getAvailableRequestHandler(keepAlive, requestMapper));
    }

    @Test
    void recycleRequestHandler() {
        int currentPoolSize = requestHandlerPool.getPoolSize();
        RequestHandler requestHandler = requestHandlerPool.getAvailableRequestHandler(keepAlive, requestMapper);
        requestHandlerPool.recycleRequestHandler(requestHandler);
        assertEquals(currentPoolSize, requestHandlerPool.getPoolSize());
    }
}