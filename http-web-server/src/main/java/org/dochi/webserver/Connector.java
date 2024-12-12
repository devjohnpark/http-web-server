package org.dochi.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ServerSocket listenSocket;
    private final ServerConfig serverConfig;


    public Connector(ServerSocket listenSocket, ServerConfig serverConfig) {
        this.listenSocket = listenSocket;
        this.serverConfig = serverConfig;
    }

    public void connect() throws IOException {
        Socket establishedSocket;
        RequestMapper requestMapper = new RequestMapper(serverConfig.getWebService().getServices());
        // webService.getServices() 가져와서 init
        // 스레드 모두 실행하고 나서 destroy
        while ((establishedSocket = listenSocket.accept()) != null) {
            SocketWrapper socketWrapper = new SocketWrapper(establishedSocket, serverConfig.getKeepAlive());
            // 추후, 스레드 풀 적용해서 스레드 재활용시 소켓 연결 끊겼으면 Socket 객체 바꿔치기: socketWrapper.changeSocket(establishedSocket);
            // 코드 재사용하기위해, RequestHandler는 Runnable 구현하도록 함
            RequestHandler requestHandler = new RequestHandler(socketWrapper, requestMapper); // RequestMapper 제공
            Thread thread = new Thread(requestHandler);
            thread.start();
        }
    }
}
