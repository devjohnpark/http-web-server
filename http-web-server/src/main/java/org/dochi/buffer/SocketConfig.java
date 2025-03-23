package org.dochi.buffer;

import org.dochi.webserver.attribute.KeepAlive;

public class SocketConfig {
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;

    public SocketConfig(KeepAlive keepAlive) {
        this.keepAliveTimeout = keepAlive.getKeepAliveTimeout();
        this.maxKeepAliveRequests = keepAlive.getMaxKeepAliveRequests();
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
}
