package org.dochi.webserver.attribute;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.HttpApiHandler;
import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.config.WebServiceConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WebService {
    private static final Path DEFAULT_ROOT_DIR = Path.of("webapp");
    private static final String rootPath = "/";

    private final Map<String, HttpApiHandler> services = new HashMap<>();
    private Path rootResourcePath = DEFAULT_ROOT_DIR;

    public WebService() {
        services.put(rootPath, new DefaultHttpApiHandler());
    }

    public void setWebResourceRootPath(String webResourceRootPath) {
        this.rootResourcePath = Path.of(webResourceRootPath);
    }

    public WebService addService(String path, HttpApiHandler service) {
        services.put(path, service);
        return this;
    }

    public Map<String, HttpApiHandler> getServices() {
        return services;
    }

    public WebServiceConfig getServiceConfig() {
        return new WebServiceConfig(new WebResourceProvider(rootResourcePath));
    }

//    public HttpApiHandler getHttpApiHandler(Path path) {
//        HttpApiHandler httpApiHandler = services.get(path);
//        if (httpApiHandler == null) {
//            return services.get(rootPath);
//        }
//        return httpApiHandler;
//    }

//    private void validateRootDirPath(Path webResourceRootPath) {
//        if (webResourceRootPath == null) {
//            throw new IllegalArgumentException("Root path cannot be null");
//        }
//        if (webResourceRootPath.startsWith("/")) {
//            throw new IllegalStateException("Root path cannot be start with: " + rootPath);
//        }
//        if (!Files.exists(webResourceRootPath) || !Files.isDirectory(webResourceRootPath)) {
//            throw new IllegalStateException("Root directory does not exist or is not a directory: " + webResourceRootPath);
//        }
//    }

//    private void validateRootDirPath(Path webResourceRootPath) {
//        if (webResourceRootPath == null) {
//            throw new IllegalArgumentException("Root path cannot be null");
//        }
//        if (webResourceRootPath.startsWith("/")) {
//            throw new IllegalArgumentException("Root directory path cannot be start with root path (/ already contains): " + webResourceRootPath);
//        }
//    }

//    private void createRootDirectory(String webResourceRootPath) {
//        File directory = new File(webResourceRootPath);
//        if (!directory.exists()) {
//            if (!directory.mkdir()) {
//                throw new RuntimeException("Can't create directory: " + directory.getAbsolutePath());
//            }
//        }
//    }

//    private void createRootDirectory(Path webResourceRootPath) {
//        try {
//            if (!Files.exists(webResourceRootPath)) {
//                Files.createDirectory(webResourceRootPath);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Can't create directory: " + webResourceRootPath.toAbsolutePath(), e);
//        }
//    }
}
