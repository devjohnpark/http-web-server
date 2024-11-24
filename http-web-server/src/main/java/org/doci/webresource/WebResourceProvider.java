package org.doci.webresource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebResourceProvider implements ResourceProvider {
    private final String rootPath;

    public WebResourceProvider(String rootPath) {
        this.rootPath = rootPath;
    }

    public final Resource getResource(String path) {
        String resourcePath = rootPath + setIfRootPath(path);
        byte[] content = readResource(resourcePath);
        ResourceType resourceType = ResourceType.fromFilePath(resourcePath);
        if (isReadContent(content) && isSupportedResourceType(resourceType)) {
            return new Resource(content, resourceType.getMimeType());
        }
        return new Resource();
    }

    private byte[] readResource(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isSupportedResourceType(ResourceType contentType) {
        return contentType != null;
    }

    private boolean isReadContent(byte[] content) {
        return content != null;
    }

    private String setIfRootPath(String filePath) {
        if (filePath.equals("/")) {
            filePath = "/index.html";
        }
        return filePath;
    }
}
