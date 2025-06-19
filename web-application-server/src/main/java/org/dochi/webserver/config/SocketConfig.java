package org.dochi.webserver.config;

public interface SocketConfig extends KeepAliveConfig {
    int getConnectionTimeout();
}
