package org.dochi.buffer;

import org.dochi.inputbuffer.socket.BioSocketWrapper;
import org.dochi.inputbuffer.socket.SocketConfig;
import org.dochi.webserver.attribute.KeepAlive;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;


// BioSocket
class BioSocketWrapperTest {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapperTest.class);

    protected byte[] clientBuffer = new byte[] { 10, 20, 30, 40, 50 };
    protected byte[] serverBuffer = new byte[clientBuffer.length];

    protected BioSocketWrapper serverConnectedSocket;
    protected BioSocketWrapper clientConnectedSocket;

    private Thread serverThread;
    private Thread clientThread;
    private ServerSocket serverSocket;

    private CountDownLatch latch;

    // 클라이언트가 요청 메세지를 보낸 시점에 서버의 스레드 실행이 종료되기 때문에
    // Connector 객체를 생성
    @BeforeEach
    void connect() throws IOException, InterruptedException {

        CountDownLatch serverReadyLatch = new CountDownLatch(1);

        serverSocket = new ServerSocket(0); // 사용 가능한 포트 자동 할당
        serverThread = new Thread(() -> {
            log.debug("Starting server...");
            if (!serverSocket.isClosed()) {
                try {

                    // 서버가 accept()를 호출하기 직전에 신호를 보냄
                    serverReadyLatch.countDown();

                    // blocking 되므로 해당 메서드를 실행하는 스레드 외의 서버 스레드로 실행
                    serverConnectedSocket = new BioSocketWrapper(serverSocket.accept(), new KeepAlive());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        serverThread.start();

        boolean ready = serverReadyLatch.await(50, TimeUnit.MILLISECONDS);

        if (!ready) {
            throw new RuntimeException("Server socket not ready");
        }

        clientConnectedSocket = new BioSocketWrapper(new Socket("localhost", serverSocket.getLocalPort()), new KeepAlive());

        log.debug("Server started.");
    }

    @AfterEach
    void disconnect() throws IOException {
        if (clientConnectedSocket != null && !clientConnectedSocket.isClosed()) {
            clientConnectedSocket.close();
            log.debug("Client socket close");
        }

        if (serverConnectedSocket != null && !serverConnectedSocket.isClosed()) {
            serverConnectedSocket.close();
            log.debug("Server socket wrapper close");
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            log.debug("Server socket close");
        }

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            log.debug("server thread interrupted");
        }
    }

//    protected void doHttp11Request(String header, String body) throws IOException {
//        if (header == null) {
//            return;
//        }
//        if (body == null) {
//            body = "";
//        }
//        doRequest(header + "\r\n" + body);
//    }

//    private void doRequest(String message) throws IOException {
//        clientBuffer = message.getBytes(StandardCharsets.ISO_8859_1);
//        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length);
//    }

    @Test
    void server_connected_socket_read() throws IOException {
        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length);
        serverConnectedSocket.read(serverBuffer, 0, serverBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__write() throws IOException {
        serverConnectedSocket.write(serverBuffer, 0, serverBuffer.length);
        clientConnectedSocket.read(clientBuffer, 0, clientBuffer.length);
        assertArrayEquals(clientBuffer, serverBuffer);
    }

    @Test
    void server_connected_socket__close() throws IOException {
        serverConnectedSocket.close();
        assertThrows(SocketException.class, () -> serverConnectedSocket.read(clientBuffer, 0, clientBuffer.length));
    }

    @Test
    void isConnected() {
        assertTrue(serverConnectedSocket.isConnected());
    }

    @Test
    void isClosed() throws IOException {
        assertFalse(serverConnectedSocket.isClosed());
        serverConnectedSocket.close();
        assertTrue(serverConnectedSocket.isClosed());
    }

    @Test
    void startConnectionTimeout_after_close() throws IOException, InterruptedException {
        serverConnectedSocket.close();
        assertThrows(SocketException.class, () -> serverConnectedSocket.startConnectionTimeout(serverConnectedSocket.getKeepAliveTimeout()));
    }

    @Test
    void startConnectionTimeout_socketTimeout() throws IOException, InterruptedException {
        int connectionTimeout = 1000;
        serverConnectedSocket.startConnectionTimeout(connectionTimeout);
        Thread readThread = new Thread(() -> {
            // SocketTimeoutException은 read가 blocking 상태일때 발생, 따라서 서버의 연결 소켓 read() 후에 connection timeout 시간이 지난뒤 클랑이언트 연결 소켓 write()
            assertThrows(SocketTimeoutException.class, () -> serverConnectedSocket.read(serverBuffer, 0, serverBuffer.length));
        });
        readThread.start();
        Thread.sleep(connectionTimeout); // 충분한 시간 대기하여 타임아웃 발생 유도
        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length); // 이후에 데이터 전송
    }
}