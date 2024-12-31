package org.dochi.webserver.config;

public class ServerConfig {
    private final KeepAlive keepAlive = new KeepAlive();
    private final WebService webService = new WebService();
    private final ThreadPool threadPool = new ThreadPool();

    public WebService getWebService() { return webService; }

    public KeepAlive getKeepAlive() { return keepAlive; }

    public ThreadPool getThreadPool() { return threadPool; }
}
