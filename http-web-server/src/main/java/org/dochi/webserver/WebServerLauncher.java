package org.dochi.webserver;

import org.dochi.http.api.LoginHttpApiHandler;
import org.dochi.webserver.executor.ServerExecutor;

import java.io.IOException;

public class WebServerLauncher {
    public static void main(String[] args) throws IOException {
        WebServer server1 = new WebServer(8080, "localhost");
        server1.getConfig().getWebService().addService("/user/create", new LoginHttpApiHandler());
        ServerExecutor.addWebServer(server1);
        ServerExecutor.execute();
    }
}
