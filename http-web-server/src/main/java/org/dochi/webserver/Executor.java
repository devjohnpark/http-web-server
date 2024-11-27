package org.dochi.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Executor {
    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    private static final Map<Integer, WebServer> webServers = new HashMap<>();

    private Executor() {}

    public static void addWebServer(WebServer webServer) {
        webServers.put(webServer.getPort(), webServer);
    }

    public static void execute() {
        List<WebServer> allWebServers = new ArrayList<>(webServers.values());
        try(ExecutorService executor = Executors.newFixedThreadPool(allWebServers.size())) {
            for (WebServer webServer: allWebServers) {
                executor.submit(webServer::start);
            }
        }
    }
}
