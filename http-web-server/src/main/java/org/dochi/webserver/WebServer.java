package org.dochi.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    private int port = 8080;
    private String hostName = "localhost";
//    private final WebService webService = new WebService();
    private final ServerConfig serverConfig = new ServerConfig();

    public WebServer() {}

    public WebServer(int port) {
        this.port = port;
    }

    public WebServer(int port, String hostName) {
        this.port = port;
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

//    public WebService getWebService() {
//        return webService;
//    }

    public ServerConfig getServerConfig() { return serverConfig; }

    public void start() {
        log.debug("Web Server started - Host: {}, Port: {}.", hostName, port);
        try(ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(hostName, port));
            Connector connector = new Connector(serverSocket, serverConfig);
            connector.connect();
        } catch (IOException e) {
            log.error("Socket accept error on the Web Server - Host: {}, Port: {}.", hostName, port);
        }
    }
}
