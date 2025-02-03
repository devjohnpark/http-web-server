package org.dochi.webserver.lifecycle;

public interface Lifecycle {

    default void start() { }

    default void stop() { }

    void init();
    void destroy();
}
