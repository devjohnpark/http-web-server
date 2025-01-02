package org.dochi.webserver.lifecycle;

import org.dochi.webserver.Connector;
import org.dochi.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ServerLifecycle extends LifecycleBase {
    private static final Logger log = LoggerFactory.getLogger(ServerLifecycle.class);

    private final WebServer webServer;

    public ServerLifecycle(WebServer webServer) {
        this.webServer = webServer;
        this.addLifeCycle(new WebServiceLifecycle(webServer.getConfig().getWebService()));
    }

    public void start() {
        super.start();
        log.info("Web Server started - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
        try(ServerSocket serverSocket = new ServerSocket()) {
            // 도메인 이름을 IP 주소로 변환해서 유효성 검증한다.
            // IP가 시스템의 네트워크 인터페이스(eth0)에 할당되어 있어야함 (ip addr로 확인가능)
            serverSocket.bind(new InetSocketAddress(webServer.getHostName(), webServer.getPort()));
            Connector connector = new Connector(serverSocket);
            connector.connect(webServer.getConfig());
        } catch (IOException e) {
            log.error("Socket accept error on the Web Server - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
        }
    }

    public void stop() {
        super.stop();
        log.info("Web Server stopped - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
    }
}
