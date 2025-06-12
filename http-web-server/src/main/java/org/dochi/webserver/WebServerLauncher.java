package org.dochi.webserver;

//import org.dochi.http.api.example.LoginHttpApiHandler;
//import org.dochi.http.api.example.UploadFileHttpApiHandler;
import org.dochi.http.api.example.LoginHttpApiHandler;
import org.dochi.http.api.example.UploadFileHttpApiHandler;
import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.executor.ServerExecutor;

import java.io.IOException;

public class WebServerLauncher {
    public static void main(String[] args) throws IOException {
        WebServer server1 = new WebServer(8080, "localhost");
        WebServer server2 = new WebServer(8080, "0.0.0.0");
        server1.getConfig().getWebService().addService("/user/create", new LoginHttpApiHandler())
                                           .addService("/upload", new UploadFileHttpApiHandler());
        ServerExecutor.addWebServer(server1);
        ServerExecutor.addWebServer(server2);
        ServerExecutor.execute();
    }
}
