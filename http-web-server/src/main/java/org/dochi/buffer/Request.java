package org.dochi.buffer;

public final class Request {
    private final MessageBytes methodMB = MessageBytes.newInstance();
    private final MessageBytes pathMB = MessageBytes.newInstance();
    private final MessageBytes uriMB = MessageBytes.newInstance();
    private final MessageBytes versionMB = MessageBytes.newInstance();
//    private final MessageBytes decodedUriMB = MessageBytes.newInstance();
    private final MimeHeaders headers = new MimeHeaders();

    public MessageBytes method() { return methodMB; }

    public MessageBytes requestURI() {
        return this.uriMB;
    }

    public MessageBytes version() { return versionMB; }

//    public MessageBytes decodedURI() {
//        return this.decodedUriMB;
//    }

    public MimeHeaders headers() { return this.headers; }

    public void recycle() {
        this.methodMB.recycle();
        this.pathMB.recycle();
        this.uriMB.recycle();
        this.versionMB.recycle();
        this.headers.recycle();
    }
}
