//package org.dochi.buffer;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.nio.charset.StandardCharsets;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class Http11InputBufferTest {
//
//    private final Request request = new Request();
//    private final Http11InputBuffer inputBuffer = new Http11InputBuffer(request, 8912);
////    private Http11InputBuffer inputBuffer;
//
//    // keep-alive를 위한 Http11InputBuffer.recycle() 테스트 가능
//    @BeforeEach
//    void setUp() {
////        request = new Request();
////        inputBuffer = new Http11InputBuffer(request, 8912);
//        inputBuffer.recycle();
////        request.recycle();
////        serverSocket = new ServerSocket(8080);
////        connectedSocket = serverSocket.accept(); // 반환값 SocketWrapper에 주입
//
////        InputStream in = socket.getInputStream(); // BioSocketWrapper.read() 호출
//
//
//
////        serverSocket = new ServerSocket(8080);
//
////        buffer = new Http11InputBuffer(new Request(), new SocketInputBuffer(new F))
//    }
//
////    private void createHttpInputBuffer(String fileName) throws IOException {
////        String testDir = "./src/test/resources/";
////        InputStream in = new FileInputStream(new File(testDir + fileName));
////        inputBuffer = new Http11InputBuffer(new Request(), new SocketInputBuffer(in), 8912);
////    }
//
//
//    // 가장 빠른 방법: Http11InputBuffer에 SocketInputBuffer에 주입
//
//    // Http11InputBuffer에서 SocketInputBuffer을 생성하는 것이 올바르다.
//    // InputBuffer 생성해서 주입해서 또 다른 InputBuffer를 생성한다면 논리적으로 말이 안된다.
//    // 따라서 Http11InputBuffer 내부에서 SocketInputBuffer을 생성
//
//    // SocketTaskHandler에서 BioSocketWrapper 주입해서 SocketInputBuffer 생성
//    // SocketTaskHandler에서 AbstractHttpProcessor.process 매개변수로 BioSocketWrapper 주입
//    // AbstractHttpProcessor에서 InputBuffer inputBuffer = new SocketInputBuffer() 생성
//    // SocketInputBuffer에 BioSocketWrapper 필요
//    // SocketWrapper에 read() 메서드 필요
//    //
//    // Http11Processor에서 Http11InputBuffer 생성
//    // SocketInputBuffer을
//
//    // Http11Processor에서 주입된 SocketInputBuffer을 Http11InputBuffer에게 set
//    // SocketInputBuffer(BioSocketWrapper)
//    // BioSocketWrapper.read(ByteBuffer buffer)
//    // BioSocketWrapper: socket.getInputStream.read(buffer.array(), buffer.position, buffer.limit())
//
//    //
////    @Test
////    void only_request_line_header_request() throws IOException {
////        String message = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\n";
//////        Request request = new Request();
////        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1));
//////        inputBuffer = new Http11InputBuffer(request, new SocketInputBuffer(byteArrayInputStream), 8912);
//////        inputBuffer = new Http11InputBuffer(request, 8912);
////        inputBuffer.init(byteArrayInputStream);
//////        inputBuffer.
////        assertFalse(inputBuffer.parseHeader());
////    }
//
//    @Test
//    void valid_request() throws IOException {
//        String message = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 11\r\n\r\n";
//
////        Request request = new Request();
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1));
////        inputBuffer = new Http11InputBuffer(request, new SocketInputBuffer(byteArrayInputStream), 8912);
////        inputBuffer = new Http11InputBuffer(request, 8912);
//        inputBuffer.init(byteArrayInputStream);
//        assertTrue(inputBuffer.parseHeader());
//        assertEquals("GET", request.method().toString());
//        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
//        assertEquals("HTTP/1.1", request.version().toString());
//        assertEquals("keep-alive", request.headers().getHeader("Connection"));
//        assertEquals("text/plain; charset=UTF-8", request.headers().getHeader("Content-Type"));
//        assertEquals("11", request.headers().getHeader("Content-Length"));
//    }
//
//    @Test
//    void duplicate_input_buffer() throws IOException {
//        String message = "GET /user?name=john%20park&password=1234 HTTP/1.1\r\nConnection: keep-alive\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Length: 11\r\n\r\n";
//
////        Request request = new Request();
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.ISO_8859_1));
////        inputBuffer = new Http11InputBuffer(request, new SocketInputBuffer(byteArrayInputStream), 8912);
////        inputBuffer = new Http11InputBuffer(request, 8912);
//        inputBuffer.init(byteArrayInputStream);
//
//        assertTrue(inputBuffer.parseHeader());
//        assertEquals("GET", request.method().toString());
//        assertEquals("/user?name=john%20park&password=1234", request.requestURI().toString());
//        assertEquals("HTTP/1.1", request.version().toString());
//        assertEquals("keep-alive", request.headers().getHeader("Connection"));
//        assertEquals("text/plain; charset=UTF-8", request.headers().getHeader("Content-Type"));
//        assertEquals("11", request.headers().getHeader("Content-Length"));
//    }
//}