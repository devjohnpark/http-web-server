package org.dochi.webserver;

import org.dochi.webserver.config.ServerConfig;
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
        // WebServiceWrapper
        // webService.getServices() 가져와서 init
        // 스레드 모두 실행하고 나서 destroy
        // 웹서버 종료되면 destroy 호출
        while ((establishedSocket = listenSocket.accept()) != null) {
            // 스레드 풀 생성시, RequestHandler, SocketWrapper 객체를 미리 생성해서 사용
            // 추후, 스레드 풀 적용해서 스레드 재활용시 소켓 연결 끊겼으면 Socket 객체 바꿔치기: socketWrapper.changeSocket(establishedSocket);
            // 코드 재사용하기위해, RequestHandler는 Thread를 extends 하는 것이 아닌 Runnable implements 하도록 변경
            SocketWrapper socketWrapper = new SocketWrapper(serverConfig.getKeepAlive());
            socketWrapper.setSocket(establishedSocket);
            RequestHandler requestHandler = new RequestHandler(socketWrapper, requestMapper); // RequestMapper 제공
            Thread thread = new Thread(requestHandler);
            thread.start();
        }
    }
}
