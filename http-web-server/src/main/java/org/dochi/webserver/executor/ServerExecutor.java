package org.dochi.webserver.executor;

import org.dochi.webserver.attribute.WebServer;
import org.dochi.webserver.lifecycle.ServerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerExecutor {
    private static final Logger log = LoggerFactory.getLogger(ServerExecutor.class);
    private static final Map<Integer, ServerLifecycle> servers = new HashMap<>();

    private ServerExecutor() {}

    public static void addWebServer(WebServer webServer) {
        if (servers.containsKey(webServer.getPort())) {
            log.error("Web server already exists: {}", webServer.getPort());
            throw new IllegalArgumentException("Web server has already exists.");
        }
        servers.put(webServer.getPort(), new ServerLifecycle(webServer));
        // 단일 서버 실행/종료를 위한 cli 대기 스레드 생성
    }

    public static void execute() {
        List<ServerLifecycle> allWebServers = new ArrayList<>(servers.values());
        if (allWebServers.isEmpty()) {
            log.error("No web servers found.");
            throw new IllegalStateException("No web servers found.");
        }
        try(ExecutorService executor = Executors.newFixedThreadPool(allWebServers.size())) {
            for (ServerLifecycle serverLifecycle : allWebServers) {
                executor.submit(serverLifecycle::start);
            }
            registerShutdownHook(allWebServers);
        }
    }

    private static void registerShutdownHook(List<ServerLifecycle> allWebServers) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ServerLifecycle serverLifecycle : allWebServers) {
                serverLifecycle.stop();
            }
        }));
    }
}
