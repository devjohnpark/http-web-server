package org.dochi.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ServerSocket listenSocket;
    private final WebService webService;


    public Connector(ServerSocket listenSocket, WebService webService) {
        this.listenSocket = listenSocket;
        this.webService = webService;
    }

    public void connect() throws IOException {
        Socket establishedSocket;
        RequestMapper requestMapper = new RequestMapper(webService.getServices());
        int threadCnt = 0;
        // webService.getServices() 가져와서 init
        // 스레드 모두 실행하고 나서 destroy
        while ((establishedSocket = listenSocket.accept()) != null) {
            threadCnt++;
            RequestHandlerTimeout2 requestHandler = new RequestHandlerTimeout2(establishedSocket, requestMapper); // RequestMapper 제공
            requestHandler.start();
            log.debug("Thread Count: {}", threadCnt);
        }
    }
}
