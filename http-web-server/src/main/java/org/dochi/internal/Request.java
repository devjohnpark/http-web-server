package org.dochi.internal;

import org.dochi.buffer.*;
import org.dochi.webserver.ApplicationBufferHandler;

import java.io.IOException;
import java.nio.charset.Charset;

// parsed raw data, low level layer
public final class Request {
    private final MessageBytes methodMB;
    private final MessageBytes requestPathMB;
    private final MessageBytes queryStringMB;
    private final MessageBytes uriMB;
    private final MessageBytes protocolMB;
    private MessageBytes contentLengthMB;
    private MessageBytes contentTypeMB;
    private final MimeHeaders headers;
    private String characterEncoding;
    private Charset charset;
    private final Parameters parameters;
    private ApplicationBufferHandler handler;
    private InputBuffer inputBuffer;

    public Request() {
//        this.inputBuffer = inputBuffer;
        this.requestPathMB = MessageBytes.newInstance();
        this.queryStringMB = MessageBytes.newInstance();
        this.methodMB = MessageBytes.newInstance();
        this.uriMB = MessageBytes.newInstance();
        this.protocolMB = MessageBytes.newInstance();
        this.headers = new MimeHeaders();
        this.parameters = new Parameters();
    }

    public MessageBytes method() { return methodMB; }

    public MessageBytes queryString() { return this.queryStringMB; }

    public MessageBytes requestPath() { return this.requestPathMB; }

    public MessageBytes requestURI() { return this.uriMB; }

    public MessageBytes protocol() { return this.protocolMB; }

    public MimeHeaders headers() { return this.headers; }

    public Parameters parameters() { return this.parameters; }

    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public InputBuffer getInputBuffer() {
        if (this.inputBuffer == null) {
            throw new IllegalStateException("Input buffer not set");
        }
        return this.inputBuffer;
    }

    public int doRead(ApplicationBufferHandler handler) throws IOException {
        return this.getInputBuffer().doRead(handler);
    }

    // 헤더의 메모리 주소를 직접 참조하여, 처음 조회 O(N) 이후에 다음번 조회시 O(1)
    public String getContentType() {
        if (this.contentTypeMB == null) {
            this.contentTypeMB = this.headers.getValue("content-type");
        }
        return this.contentTypeMB.toString();
    }

    public int getContentLength() {
        if (this.contentLengthMB == null) {
            this.contentLengthMB = this.headers.getValue("content-length");
        }
        return this.contentLengthMB.toInt();
    }

    public Charset getCharsetFromContentType() {
        if (this.charset == null) {
            this.getCharacterEncoding();
            if (this.characterEncoding != null) {
                this.charset = Charset.forName(this.characterEncoding);
            }
        }
        return this.charset;
    }

    public String getHeader(String name) {
        return this.headers.getHeader(name);
    }

    public String getCharacterEncoding() {
        if (this.characterEncoding == null) {
            this.characterEncoding = getCharsetEncodingFromContentType(this.getContentType());
        }
        return this.characterEncoding;
    }

    private String getCharsetEncodingFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        MediaType mediaType = MediaType.parseMediaType(contentType);
        if (mediaType != null) {
            return mediaType.getCharset();
        }
        return "";
    }

    public void recycle() {
        this.methodMB.recycle();
        this.requestPathMB.recycle();
        this.queryStringMB.recycle();
        this.uriMB.recycle();
        this.protocolMB.recycle();
        this.headers.recycle();
        this.parameters.recycle();
        this.contentLengthMB = null;
        this.contentTypeMB = null;
        this.characterEncoding = null;
        this.charset = null;
    }
}
