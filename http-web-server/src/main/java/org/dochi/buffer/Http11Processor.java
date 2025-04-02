//package org.dochi.buffer;
//
//import org.dochi.buffer.internal.Adapter;
//import org.dochi.http.request.data.HttpVersion;
//import org.dochi.http.request.data.RequestHeaders;
//import org.dochi.webserver.socket.SocketState;
//import org.dochi.webserver.socket.SocketWrapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import static org.dochi.webserver.socket.SocketState.*;
//
//public class Http11Processor extends AbstractHttpProcessor {
//    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);
//
////    public Http11Processor(SocketWrapperBase<?> socketWrapper, HttpConfig config) {
//////        super(
//////                new Http11InputBuffer(z, config.getHttpReqConfig().getRequestHeaderMaxSize()),
//////                new Http11ResponseProcessor(out, config.getHttpResConfig())
//////        );
////
////
////    }
//
//    private final Http11InputBuffer inputBuffer;
//
//    // Http11InputBuffer를 connector.Request.InternalInputStream의 ByteBuffer에게 복사
//    // internal.Requset.setInputBuffer() 한번만 호출
//    //
//
//    public Http11Processor(Adapter internalAdapter) {
//        super(internalAdapter);
//        this.inputBuffer = new Http11InputBuffer(8192);
//        request.setInputBuffer(inputBuffer);
//    }
//
//    public boolean shouldKeepAlive(SocketWrapper socketWrapper) {
//        return isRequestKeepAlive() && isSeverKeepAlive(socketWrapper);
//    }
//
//    private boolean shouldNext(SocketWrapper socketWrapper) {
//        boolean isKeepAlive = shouldKeepAlive(socketWrapper);
//        response.addConnection(isKeepAlive);
//        if (isKeepAlive) {
//           response.addKeepAlive(socketWrapper.getKeepAliveTimeout(), socketWrapper.getMaxKeepAliveRequests());
//        }
//        return isKeepAlive;
//    }
//
//    private boolean isSeverKeepAlive(SocketWrapper socketWrapper) {
//        return !isReachedMax(socketWrapper.incrementKeepAliveCount(), socketWrapper.getMaxKeepAliveRequests());
//    }
//
//    private static boolean isReachedMax(int currentCount, int maxCount) {
//        return currentCount >= maxCount;
//    }
//
//    private boolean isRequestKeepAlive() {
//        String connectionValue = request.getConnection();
//        if (request.getHttpVersion().equals(HttpVersion.HTTP_1_1)) {
//            return !(connectionValue != null && connectionValue.equals("close"));
//        }
//        return request.getHttpVersion().equals(HttpVersion.HTTP_1_0) && (connectionValue != null && connectionValue.equals("keep-alive"));
//    }
//
//    private boolean isUpgradeRequest(SocketWrapper socketWrapper) {
//        return request.getHeader(RequestHeaders.UPGRADE) != null;
//    }
//
//    // internalAdapter.service(data.Rquest, data.Response)
//    // ddatpter.
//    @Override
//    protected SocketState service(SocketWrapperBase<?> socketWrapper) {
//        SocketState state = OPEN;
//        int processCount = 0;
//        try {
////            request.recycle(); // inputbuffer도 recycle
////            response.
//            recycle();
//
//            // Recycle Logic: high level -> low level
//            // 1. Before header parsing: internal.Request.recycle() -> InputBuffer.recycle()
//            // 2. After header parsing: connector.Request.recycle() -> internal.Request.recycle() & InternalInputStream.recycle()
//
//            // Recycle 로직은 생성자의 매개변수로 받아들이는 객체나 생성자 내에서 생성되는 객체의 recycle를 수행한다.
//
//
//            // internal.Request: low level 데이터를 저장 (프로토콜별 데이터를 해석하는 InputBuffer 포함)
//            // Recycle: request.recycle() -> inputbuffer.recycl()
//
//            // connector.Request: developer에게 제공하는 공통화된 요청 처리 객체
//            // Recycle: request.recycle() ->
//
//            // Invalid header parsing
//            // internal.Request.recycle()
//            // -> InputBuffer.recycle()
//
//            // Valid header parsing
//            // connector.Request.recycle()
//            // -> internal.Request.recycle()
//            // -> internal.InternalInputStream.recycle()
//            //      -> InputBuffer.recycle()
//
//
//            while (state == OPEN) {
//                if (!inputBuffer.parseHeader(request)) {
//                    request.recycle(); // memory visibility
//                    // Recycling object's sharing resource cannot match the main memory with cpu cache in multithreading environment.
//                    // I choose recycling object initialization cuz volatile variable for memory visibility has overhead.
//                    state = CLOSED;
//                    break;
//                } else if (isUpgradeRequest(socketWrapper)) {
//                    // Current ignore HTTP/1.1 upgrade request, processing as HTTP/1.1 (Later support HTTP/2.0)
//                    state = UPGRADING;
//                    // 1. upgradeToken(); // upgradeToken = getHeader(Upgrade) & getHeader(HTTP2-Settings);
//                    // 2. sendUpgrade(); // HTTP/1.1 response 101 status
//                    // 3. break;
//                    // After client preface request -> response as HTTP/2.0 using Http2Processor
//                } else if (!shouldNext(socketWrapper)) {
//                    state = CLOSED;
//                }
//
//                // adapter에서 connector.Request/Response.recycle() 호출
//                httpApiMapper.getHttpApiHandler(request.getPath()).service(request, response);
//
//                // response.flush()
//                // Response object provides OutputStream object to developer, so it need flush() after processing HTTP API
//                // flush() has system call cost, it needs to remove inefficient action.
//                // 1. Rapping flush method by custom OutputStream.
//                // 2. The custom OutputStream declares boolean-isFlushed variable.
//                // 3. If call rapped flush method, According to isFlushed value(true/false), flush() to be called or not.
////                recycle();
//
//                inputBuffer.recycle();
//                outputBuffer.recycle();
//
//
//
//                processCount++;
//            }
//        } catch (Exception e) {
//            processException(e);
////            request.recycle(); // inputbuffer도 recycle
////            response.recycle(); // Ou
//            safeRecycle();
//            state = CLOSED;
//        }
//        log.debug("Processed keep-alive requests count: {}", processCount);
//        return state;
//    }
//
//
////    private void sendUpgrade() {
////
////    }
//}
//
