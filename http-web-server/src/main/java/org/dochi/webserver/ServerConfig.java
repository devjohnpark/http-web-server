package org.dochi.webserver;

public class ServerConfig {
    private final KeepAlive keepAlive = new KeepAlive();
    private final WebService webService = new WebService();
//    private final SocketWrapper socketWrapper = new SocketWrapper();

    public WebService getWebService() {
        return webService;
    }
    public KeepAlive getKeepAlive() { return keepAlive; }

//    public SocketWrapper getSocketWrapper() { return socketWrapper; }
}
