package org.dochi.webserver.config;

public interface KeepAliveConfig {
    int getKeepAliveTimeout();
    int getMaxKeepAliveRequests();
}
