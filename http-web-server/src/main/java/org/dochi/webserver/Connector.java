package org.dochi.webserver;

import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.executor.WorkerPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ServerSocket listenSocket;

    public Connector(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
    }

    public void connect(ServerConfig serverConfig) throws IOException {
        RequestMapper requestMapper = new RequestMapper(serverConfig.getWebService().getServices());
        WorkerPoolExecutor workerPoolExecutor = new WorkerPoolExecutor(serverConfig.getThreadPool());
        RequestHandlerPool requestHandlerPool = new RequestHandlerPool(serverConfig.getThreadPool().getMinSpareThreads(), serverConfig.getKeepAlive(), requestMapper);

        Socket establishedSocket;
        while ((establishedSocket = listenSocket.accept()) != null) {
            // 사용가능한 RequestHandler 객체 가져오기
            RequestHandler requestHandler = requestHandlerPool.getAvailableRequestHandler(serverConfig.getKeepAlive(), requestMapper);

            // 새롭게 연결된 소켓 설정
            requestHandler.getSocketWrapper().setSocket(establishedSocket);

            // requestHandler의 run() 실행 후 requestHandlerPool에 requestHandler 반환
            workerPoolExecutor.executeRequest(requestHandler, requestHandlerPool);
        }
    }
}
