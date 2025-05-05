//package org.dochi.webserver.socket;
//
//import org.dochi.inputbuffer.socket.BioSocketWrapper;
//import org.dochi.inputbuffer.socket.SocketConfig;
//import org.dochi.inputbuffer.socket.SocketWrapperBase;
//import org.dochi.webserver.attribute.KeepAlive;
//import org.junit.jupiter.api.Test;
//
//import org.junit.jupiter.api.*;
//import java.io.*;
//import java.net.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class SocketWrapperBaseTest {
//    // BioEndpoint 내에 ServerSocket 객채 감춘다.
//    // BioEndpoint extends AbstractEndpoint<U>
//    // AbstractEndpoint 내에 Acceptor<U> acceptor, SocketWrapperBase<U> socketWrapper
//    private static ServerSocket serverSocket;
//    private SocketWrapperBase<Socket> socketWrapper;
//
//    // 테스트 클래스 내의 모든 테스트가 실행되기 전에 딱 한 번 ServerSocket.accept() 메서드를 실행
//    // NIO 확장성을 위해서는 서버 소켓의 적용이 필요함
//    // 클라이언트가 요청 메세지를 보내면 스레드 실행이 종료됨
//    @BeforeAll
//    static void startServer() throws IOException {
//        System.out.println("Starting server...");
//        serverSocket = new ServerSocket(0); // 사용 가능한 포트 자동 할당
//
//        new Thread(() -> {
//            while (!serverSocket.isClosed()) { // 여러 클라이언트 요청 처리 가능하도록 변경
//                try (Socket socket = serverSocket.accept(); OutputStream out = socket.getOutputStream()) {
//                    out.write(new byte[] { 10, 20, 30, 40, 50 });
//                    out.flush();
//                } catch (IOException e) {
//                    if (!serverSocket.isClosed()) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }).start();
//    }
//
//    // 테스트의 무결성을 위해 각 메서드마다 새로운 클라 연결 생성
//    @BeforeEach
//    void setUp() throws IOException {
//        // 클라이언트 소켓을 서버에 연결
//        socketWrapper = new BioSocketWrapper(new SocketConfig(new KeepAlive()));
//        socketWrapper.setConnectedSocket(new Socket("localhost", serverSocket.getLocalPort()));
//    }
//
//    @Test
//    void testRead1() throws IOException {
//        byte[] buffer = new byte[5];
//        int bytesRead = socketWrapper.read(buffer, 1, 3);
//
//        assertEquals(3, bytesRead);
//        assertEquals(0, buffer[0]);
//        assertEquals(10, buffer[1]);
//        assertEquals(20, buffer[2]);
//        assertEquals(30, buffer[3]);
//        assertEquals(0, buffer[4]);
//    }
//
//    @Test
//    void testRead2() throws IOException {
//        byte[] buffer = new byte[5];
//        int bytesRead = socketWrapper.read(buffer, 1, 3);
//
//        assertEquals(3, bytesRead);
//        assertEquals(0, buffer[0]);
//        assertEquals(10, buffer[1]);
//        assertEquals(20, buffer[2]);
//        assertEquals(30, buffer[3]);
//        assertEquals(0, buffer[4]);
//    }
//
//    // 테스트의 무결성을 위해 각 메서드마다 새로운 클라 연결 종료
//    @AfterEach
//    void tearDown() throws IOException {
//        socketWrapper.close();
//    }
//
//    @AfterAll
//    static void stopServer() throws IOException {
//        serverSocket.close();
//        System.out.println("Stop server...");
//    }
//}