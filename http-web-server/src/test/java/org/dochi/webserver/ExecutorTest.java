package org.dochi.webserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {
    @Test
    void addWebServer() {
        Executor.addWebServer(new WebServer(8080));
        assertThrows(IllegalArgumentException.class, () -> Executor.addWebServer(new WebServer(8080)));
    }
}