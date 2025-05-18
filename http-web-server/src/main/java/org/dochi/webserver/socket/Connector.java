package org.dochi.webserver.socket;

import org.dochi.inputbuffer.socket.BioSocketWrapper;
import org.dochi.webserver.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector {
    private static final Logger log = LoggerFactory.getLogger(Connector.class);
    private final ServerSocket listenSocket;

    public Connector(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
    }

    public void connect(SocketTaskExecutor socketTaskExecutor, String hostName, int port, ServerConfig config) throws IOException {
        // 도메인 이름을 IP 주소로 변환해서 유효성 검증한다.
        // IP가 시스템의 네트워크 인터페이스(eth0)에 할당되어 있어야함 (ip addr로 확인가능)
        listenSocket.bind(new InetSocketAddress(hostName, port));
        log.info("Listening client connection request [Host: {}, Port: {}]", hostName, port);
        Socket establishedSocket;
        while ((establishedSocket = listenSocket.accept()) != null) {
            log.info("Accepted new client connection [Client IP: {}, Port: {}]", establishedSocket.getInetAddress(), establishedSocket.getPort());
            socketTaskExecutor.execute(new BioSocketWrapper(establishedSocket, config.getKeepAlive()));
        }
    }
}
