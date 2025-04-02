//package org.dochi.buffer;
//
//import org.dochi.buffer.internal.Adapter;
//import org.dochi.buffer.internal.Request;
//import org.dochi.buffer.internal.Response;
//import org.dochi.http.exception.HttpStatusException;
//import org.dochi.http.response.HttpStatus;
//import org.dochi.webserver.socket.SocketState;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.SocketException;
//import java.net.SocketTimeoutException;
//
//import static org.dochi.webserver.socket.SocketState.CLOSED;
//
//// 1. AbstractHttpProcessor: Request, Response 객체를 생성
//// 2. AbstractHttpProcessor 구현체의 service(Request, Response, Adapter) 호출
//// 3. Adapter 객체가 HttpApiRequest 구현체와 HttpApiResponse 구현체에 각각 Request, Response 객체 주입
//// 4. HttpApiRequest 구현체에서 this.inputBuffer.setRequest(Request), InputBuffer로 멀티 파트등의 본문 파싱을 수행한 뒤에 Request에 해당 정보 저장 가능하기 위해서
//// 4. Adapter 객체가 Request의 객체의 path를 확인해서 부합하는 HttpApiHandler 찾아낸다.
//// 5. 찾아낸 HttpApiHandler 객체의 service(HttpApiRequest, HttpApiResponse) 메서드를 객체를 넘겨서 호출
//public abstract class AbstractHttpProcessor implements HttpProcessor {
//    private static final Logger log = LoggerFactory.getLogger(AbstractHttpProcessor.class);
////    protected final HttpApiMapper httpApiMapper;
//    protected final org.dochi.buffer.internal.Request request;
//    protected final Response response;
//    protected final Adapter internalAdapter;
//
//    protected AbstractHttpProcessor(Adapter internalAdapter) {
//        this.request = new Request();
//        this.response = new Response();
//        this.internalAdapter = internalAdapter;
//    }
//
//    @Override
//    public SocketState process(SocketWrapperBase<?> socketWrapper) {
//        SocketState state = CLOSED;
//        try {
//            socketWrapper.startConnectionTimeout(socketWrapper.getKeepAliveTimeout());
//            state = service(socketWrapper, httpApiMapper);
//        } catch (SocketException e) {
//            log.error("Call setSoTimeout() but socket is already closed: {}", e.getMessage());
//            sendError(HttpStatus.BAD_REQUEST, "socket is already closed");
//        }
//        return state;
//    }
//
//    protected abstract SocketState service(SocketWrapperBase<?> socketWrapper);
//
//    // clear resource of request and response
//    protected void recycle() throws IOException {
//        request.recycle();
//        response.recycle();
//    }
//
//    protected void safeRecycle() {
//        try {
//            recycle();
//        } catch (IOException e) {
//            log.error("Recycle failed: ", e);
//        }
//    }
//
//    // Because the developer has the option to handle RuntimeException, RuntimeException propagated by not catching it is considered to be an invalid request from the client and a 400 response is sent.
//    // Unexpected IOException on input/output, 500 response because Exception is a server problem.
//    protected void processException(Exception e) {
//        switch (e) {
//            case SocketTimeoutException socketTimeoutException -> {
////                After setting the input time with soSetTimeout() on a Socket, a timeout occurs while the server is reading the request: 408 Request Timeout response (include 'Connection: close' header)
////                SocketTimeoutException exception thrown when valid time expires while being blocked by read() method of SocketInputStream object (write() is not related to setSoTimeout)
////                A SocketTimeoutException is thrown if the client has not read all the data in the socket receive buffer while receiving all the data from the web server after sending the request.
////                Therefore, if you read the data in the socket buffer multiple times through system calls, you must give the client a response before the client realizes it.
//                log.error("Socket read timeout occurred: ", e);
//                sendError(HttpStatus.REQUEST_TIMEOUT, e.getMessage());
//            }
//            case SocketException socketException -> {
//                // reference: NioSocketImpl.implRead()
//                //  If call Socket.read() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Connection reset") internally in Socket
//                //  If call Socket.write() after client close the socket after the client close the socket, occurred a situation that throws SocketException("Socket closed") internally in Socket
//                log.error("Socket was read or write after the client closed connection: ", e);
//            }
//            case HttpStatusException httpStatusException -> {
//                sendError(httpStatusException.getHttpStatus(), e.getMessage());
//            }
//            case RuntimeException runtimeException -> {
//                sendError(HttpStatus.BAD_REQUEST, e.getMessage());
//            }
//            case null, default -> { // IOException, Exception
//                assert e != null;
//                sendError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//            }
//        }
//    }
//
//    private void sendError(HttpStatus status, String errorMessage) {
//        log.error("HTTP status: {} {}, Reason: {}", String.valueOf(status.getCode()), status.getMessage(), errorMessage);
//        try {
//            if (status.getCode() >= 500) {
//                // response.setStatus(status)
//                // HttpXXProcessor.outbuffer.flush()
//                response.sendError(status, status.getMessage());
//            } else if (status.getCode() >= 400) {
//                response.sendError(status, errorMessage);
//            }
//        } catch (IOException e) {
//            log.error("Failed to send error response: {}", e.getMessage());
//        }
//    }
//}
