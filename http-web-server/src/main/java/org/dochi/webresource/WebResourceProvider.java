package org.dochi.webresource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebResourceProvider implements ResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(WebResourceProvider.class);
    private static final String DEFAULT_PAGE = "index.html";
    private final Path rootDirPath;

    public WebResourceProvider(Path rootDirPath) {
        this.rootDirPath = createRootDirectory(validateRootDirPath(rootDirPath));
    }

    public Resource getResource(String resourcePath) {
        // 절대 경로 ('/'로 시작): webapp.resolve(/index.html) -> /index.html
        // 상대 경로 ('/'로 시작 안함): webapp.resolve(index.html) -> webapp/index.html
        return getResourceInternal(rootDirPath.resolve(normalizeFilePath(validateResourcePath(resourcePath))));
    }

    private String normalizeFilePath(String filePath) {
        if (filePath.equals("/")) {
            filePath += DEFAULT_PAGE;
        }
        return filePath.startsWith("/") ? filePath.substring(1) : filePath;
    }

    private Resource getResourceInternal(Path resourcePath) {
        return new Resource(readResource(resourcePath), ResourceType.fromFilePath(resourcePath).getMimeType());
    }

    private byte[] readResource(Path path) {
        if (!Files.exists(path)) {
            log.error("Resource not found: {}", path);
            return null;
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Failed to read resource: {}", path);
            return null;
        }
    }

    /*
    Java Compiler가 Checked Exception은 컴파일 타임에 예외를 처리할수 있도록 명시하는 것을 강제한다. 그래서 throws가 없으면 컴파일 오류 발생하게 된다.
    따라서 Checked Exception는 메서드에 throws가 명시되어있어야지 Call Stack 상위로 예외가 전파된다.

    반면에 Unchecked Exception은 Java Compiler가 컴파일 타임에 예외를 처리할수 있도록 명시했는지 검사하지않는다.
    따라서 throws 없이도 Call Stack 상위로 예외가 전파된다.

    그래서 예외를 처리하는 HttProcessor의 메서드까지 예외가 전파되서 클라이언트에게 응답을 보내도록 했다.
     */
    private Path validateRootDirPath(Path webResourceRootPath) {
        if (webResourceRootPath == null) {
            throw new IllegalArgumentException("Root directory path cannot be null");
        }
        if (webResourceRootPath.startsWith("/")) {
            throw new IllegalArgumentException("Root directory path cannot be start with root path / already contains): " + webResourceRootPath);
        }
        if (!Files.exists(webResourceRootPath) || !Files.isDirectory(webResourceRootPath)) {
            throw new IllegalArgumentException("Root directory does not exist or is not a directory: " + webResourceRootPath);
        }
        return webResourceRootPath;
    }

    private Path createRootDirectory(Path webResourceRootPath) {
        try {
            if (!Files.exists(webResourceRootPath)) {
                Files.createDirectory(webResourceRootPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't create directory: " + webResourceRootPath.toAbsolutePath(), e);
        }
        return webResourceRootPath;
    }

    private String validateResourcePath(String resourcePath) {
        if (resourcePath == null) {
            throw new IllegalArgumentException("Resource path cannot be null");
        }
        if (resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be empty");
        }
        return resourcePath;
    }
}