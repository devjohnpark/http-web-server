package org.dochi.webserver;

import org.dochi.webserver.context.ServerContext;
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
    private static final Map<Integer, ServerContext> servers = new HashMap<>();

    private Executor() {}

    public static void addWebServer(WebServer webServer) {
        if (servers.containsKey(webServer.getPort())) {
            log.error("Web server already exists: {}", webServer.getPort());
            throw new IllegalArgumentException("Web server has already exists.");
        }
        servers.put(webServer.getPort(), new ServerContext(webServer));
    }

    public static void execute() {
        List<ServerContext> allWebServers = new ArrayList<>(servers.values());
        try(ExecutorService executor = Executors.newFixedThreadPool(allWebServers.size())) {
            for (ServerContext serverContext: allWebServers) {
                executor.submit(serverContext::start);
            }
            registerShutdownHook(allWebServers);
        }
    }

    private static void registerShutdownHook(List<ServerContext> allWebServers) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Execute hook just before jvm shutdown.");
            for (ServerContext serverContext: allWebServers) {
                serverContext.stop();
            }
        }));
    }
}
