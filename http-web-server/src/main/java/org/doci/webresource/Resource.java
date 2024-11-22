package org.doci.webresource;

public class Resource {
    private byte[] data;
    private String contentType;

    public Resource() {}

    public Resource(byte[] data, String format) {
        this.data = data;
        this.contentType = format;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isEmpty() {
        return data == null || contentType == null;
    }
}