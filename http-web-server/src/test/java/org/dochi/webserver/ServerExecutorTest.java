package org.dochi.webserver;

import org.dochi.webserver.executor.ServerExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerExecutorTest {
    @Test
    void addWebServer() {
        ServerExecutor.addWebServer(new WebServer(8080));
        assertThrows(IllegalArgumentException.class, () -> ServerExecutor.addWebServer(new WebServer(8080)));
    }
}