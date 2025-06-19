package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.dochi.webserver.config.*;
import org.dochi.webserver.executor.WorkerPoolExecutor;

public class SocketTaskExecutorFactory {
    private static final SocketTaskExecutorFactory INSTANCE = new SocketTaskExecutorFactory();

    private SocketTaskExecutorFactory() {}

    public static SocketTaskExecutorFactory getInstance() {
        return INSTANCE;
    }

    public SocketTaskExecutor createExecutor(ServerConfig serverConfig) {
        HttpApiMapper httpApiMapper = new HttpApiMapper(serverConfig.getWebService());
        WorkerPoolExecutor workerExecutor = new WorkerPoolExecutor(serverConfig.getThreadPool());
        HttpConfig httpConfig = new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());
        HttpProtocolHandler protocolHandler = new HttpProtocolHandler(httpConfig);
        SocketTaskPool taskPool = createTaskPool(serverConfig, protocolHandler, httpApiMapper);
        return new SocketTaskExecutor(workerExecutor, taskPool);
    }

    private SocketTaskPool createTaskPool(
            ServerConfig serverConfig,
            HttpProtocolHandler protocolHandler,
            HttpApiMapper httpApiMapper) {
        return new SocketTaskPool(
                serverConfig.getThreadPool(),
                () -> new SocketTaskHandler(
                        protocolHandler,
                        httpApiMapper
                )
        );
    }
}
