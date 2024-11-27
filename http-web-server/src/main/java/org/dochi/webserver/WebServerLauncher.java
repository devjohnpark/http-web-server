package org.dochi.webserver;

import org.dochi.http.api.LoginHttpApiHandler;

import java.io.IOException;

public class WebServerLauncher {
    public static void main(String[] args) throws IOException {
        WebServer server1 = new WebServer(8080, "localhost");
        server1.getWebService().addService("/user/create", new LoginHttpApiHandler());

        WebServer server2 = new WebServer(7070, "localhost");

        Executor.addWebServer(server1);
        Executor.addWebServer(server2);
        Executor.execute();
    }
}
