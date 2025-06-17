package org.dochi.webserver.executor;

import org.dochi.webserver.attribute.WebServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerExecutorTest {

    @Test
    void duplication_addWebServer() {
        WebServer webServer1 = new WebServer(8080, "localhost");
        WebServer webServer2 = new WebServer(8080, "localhost");

        ServerExecutor.addWebServer(webServer1);
        assertThrows(IllegalArgumentException.class, () -> ServerExecutor.addWebServer(webServer2));
    }
}