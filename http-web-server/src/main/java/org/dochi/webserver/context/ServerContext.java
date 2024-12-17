package org.dochi.webserver.context;

// Webserver.start() -> WebServiceContext.init()
// Webserver.stop() -> WebServiceContext.destroy()

import org.dochi.webserver.Connector;
import org.dochi.webserver.lifecycle.LifecycleBase;
import org.dochi.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ServerContext extends LifecycleBase {
    private static final Logger log = LoggerFactory.getLogger(ServerContext.class);

    private final WebServer webServer;

    public ServerContext(WebServer webServer) {
        this.webServer = webServer;
        this.addLifeCycle(new WebServiceContext(webServer.getServerConfig().getWebService()));
    }

    public void start() {
        super.start();
        log.info("Web Server started - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
        try(ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(webServer.getHostName(), webServer.getPort()));
            Connector connector = new Connector(serverSocket);
            connector.connect(webServer.getServerConfig());
        } catch (IOException e) {
            log.error("Socket accept error on the Web Server - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
        }
    }

    public void stop() {
        super.stop();
        log.info("Web server shutdown - Host: {}, Port: {}.", webServer.getHostName(), webServer.getPort());
    }
}
