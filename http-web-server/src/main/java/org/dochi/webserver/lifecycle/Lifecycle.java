package org.dochi.webserver.lifecycle;

public interface Lifecycle {
    void start();
    void stop();
    void init();
    void destroy();
}
