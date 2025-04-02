package org.dochi.buffer.internal;

import org.dochi.buffer.InputBuffer;
import org.dochi.buffer.MessageBytes;
import org.dochi.buffer.MimeHeaders;
import org.dochi.buffer.Parameters;

public final class Request {
    private final MessageBytes methodMB;
    private final MessageBytes queryMB;

    // request-uri 파싱은 누가 어떻게 할것인가?
    //
    private final MessageBytes uriMB;
//    private final MessageBytes decodedUriMB = MessageBytes.newInstance();
    private final Parameters parameters;
    private final MessageBytes protocolMB;
    private final MimeHeaders headers;

    private final InputBuffer inputBuffer;


    // /user/create?userId=john&password=1234
    // ? 기준점으로 path/query으로 파싱한다.
    // query를 parameter를 통해 = 기준으로 key, value로 파싱해서 hashmap<String, String>에 저장한다.

    public Request(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
        this.methodMB = MessageBytes.newInstance();
        this.uriMB = MessageBytes.newInstance();
        this.protocolMB = MessageBytes.newInstance();
        this.headers = new MimeHeaders();


        this.queryMB = MessageBytes.newInstance();

        this.parameters = new Parameters();
        this.parameters.setQuery(this.queryMB);
    }

    public MessageBytes method() { return methodMB; }

    public MessageBytes queryString() {
        return this.queryMB;
    }

    // /user/create?userId=john&password=1234
    // request path인 /user/crate는 HttpRequest 구현체에 저장
    // query string인 userId=john&password=1234는 queryMB에 저장
    // request paramters인 userId=john는
    public MessageBytes requestURI() {
        return this.uriMB;
    }



//    public MessageBytes path() { return requestPathMB; }

    public MessageBytes protocol() {
        return this.protocolMB;
    }

//    public MessageBytes decodedURI() {
//        return this.decodedUriMB;
//    }

    public MimeHeaders headers() { return this.headers; }

    public Parameters parameters() {
        return this.parameters;
    }



//    public void setInputBuffer(InputBuffer inputBuffer) {
//        this.inputBuffer = inputBuffer;
//    }

    public InputBuffer getInputBuffer() {
        if (this.inputBuffer == null) {
            throw new IllegalStateException("Input buffer not set");
        }
        return this.inputBuffer;
    }

//
//    public int doRead(ByteBuffer buffer) throws IOException {
////        if (this.getBytesRead() == 0L && !this.response.isCommitted()) {
////            this.action(ActionCode.ACK, ContinueResponseTiming.ON_REQUEST_BODY_READ);
////        }
//
//        int n = this.inputBuffer.doRead(buffer);
//        if (n > 0) {
//            this.bytesRead += (long) n;
//        }
//
//        return n;
//    }
//

    public void recycle() {
        this.methodMB.recycle();
//        this.requestPathMB.recycle();
        this.uriMB.recycle();
        this.protocolMB.recycle();
        this.headers.recycle();
        this.inputBuffer.recycle();
    }
    
    
}
