package org.dochi.webserver.config;

public class KeepAlive {
    private int keepAliveTimeout = 3000; // -1: unlimited
    private int maxKeepAliveRequests = 10000; // -1: unlimited

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        if (keepAliveTimeout < 0) {
            throw new IllegalArgumentException("keepAliveTimeout cannot be negative");
        }
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        if (maxKeepAliveRequests < 0) {
            throw new IllegalArgumentException("maxKeepAliveRequests cannot be negative");
        }
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
}
