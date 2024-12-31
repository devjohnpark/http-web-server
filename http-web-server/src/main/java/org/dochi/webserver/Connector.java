package org.dochi.webserver;

import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.executor.ThreadPoolExecutor;
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
        Socket establishedSocket;
        RequestMapper requestMapper = new RequestMapper(serverConfig.getWebService().getServices());
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(serverConfig.getThreadPool());
        threadPoolExecutor.initThreadPool(serverConfig, requestMapper);
        while ((establishedSocket = listenSocket.accept()) != null) {
//            SocketWrapper socketWrapper = new SocketWrapper(serverConfig.getKeepAlive());
//            socketWrapper.setSocket(establishedSocket);
//            RequestHandler requestHandler = new RequestHandler(socketWrapper, requestMapper); // RequestMapper 제공
//            Thread thread = new Thread(requestHandler);
//            thread.start();
            threadPoolExecutor.executeRequestHandler(establishedSocket, requestMapper, serverConfig);
        }
    }
}
