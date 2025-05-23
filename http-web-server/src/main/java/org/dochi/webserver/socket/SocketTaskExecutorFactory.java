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
        HttpProtocolHandler protocolHandler = new HttpProtocolHandler(createHttpConfig(serverConfig), serverConfig.getHttpProcessorAttribute());
        SocketTaskPool taskPool = createTaskPool(serverConfig, protocolHandler, httpApiMapper);
        return new SocketTaskExecutor(workerExecutor, taskPool);
    }

    private HttpAttribute createHttpConfig(ServerConfig serverConfig) {
        return new HttpAttribute(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());
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
