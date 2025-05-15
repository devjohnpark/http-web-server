package org.dochi.webserver.attribute;

public class HttpReqAttribute {
    private static final int DEFAULT_HEADER_MAX_SIZE = 8192;
    private static final int DEFAULT_BODY_MAX_SIZE = 2097152;

    private int requestHeaderMaxSize = DEFAULT_HEADER_MAX_SIZE;
    private int requestPayloadMaxSize = DEFAULT_BODY_MAX_SIZE;

    public void setRequestPayloadMaxSize(int requestPayloadMaxSize) {
        this.requestPayloadMaxSize = requestPayloadMaxSize;
    }

    public void setRequestHeaderMaxSize(int requestHeaderMaxSize) {
        this.requestHeaderMaxSize = requestHeaderMaxSize;
    }

    public int getRequestPayloadMaxSize() {
        return requestPayloadMaxSize;
    }

    public int getRequestHeaderMaxSize() {
        return requestHeaderMaxSize;
    }
}
