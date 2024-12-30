package org.dochi.webserver.lifecycle;

public interface Lifecycle {
    void init();
    void start();
    void stop();
    void destroy();
}
