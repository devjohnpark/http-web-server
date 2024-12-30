package org.dochi.webserver.config;

public class KeepAlive {
    private int keepAliveTimeout = 1000; // default: 1000 ms
    private int maxKeepAliveRequests = 60; // default: 60

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        if (keepAliveTimeout <= 0) {
            throw new IllegalArgumentException("keepAliveTimeout cannot be negative or zero.");
        }
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        if (maxKeepAliveRequests <= 0) {
            throw new IllegalArgumentException("maxKeepAliveRequests cannot be negative or zero.");
        }
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }
}
