package org.dochi.webresource;

public enum ResourceType {
    TEXT(".txt", "text/plain"),
    HTML(".html", "text/html"),
    JS(".js", "application/javascript"),
    XML(".xml", "application/xml"),
    CSS(".css", "text/css"),
    PNG(".png", "image/png"),
    ICO(".ico", "image/x-icon"),
    JPEG(".jpeg", "image/jpeg"),
    JSON(".json", "application/json"),
    URL(".url", "application/x-www-form-urlencoded");

    private final String extension;
    private final String contentType;

    ResourceType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        if (this == TEXT || this == HTML || this == JS || this == XML || this == CSS) {
            return contentType + "; charset=UTF-8";
        }
        return contentType;
    }

    public static ResourceType fromFilePath(String filePath) {
        for (ResourceType fileType : values()) {
            if (filePath.endsWith(fileType.getExtension())) {
                return fileType;
            }
        }
        return null;
    }
}