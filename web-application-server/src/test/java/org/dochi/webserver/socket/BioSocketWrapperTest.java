package org.dochi.webserver.socket;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class BioSocketWrapperTest extends BioSocketWrapperConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapperTest.class);

    protected byte[] clientBuffer = new byte[] { 10, 20, 30, 40, 50 };
    protected byte[] serverBuffer = new byte[clientBuffer.length];

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
        assertThrows(SocketException.class, () -> serverConnectedSocket.setConnectionTimeout(serverConnectedSocket.getConfigKeepAliveTimeout()));
    }

    @Test
    void startConnectionTimeout_socketTimeout() throws IOException, InterruptedException {
        int connectionTimeout = 1000;
        serverConnectedSocket.setConnectionTimeout(connectionTimeout);
        Thread readThread = new Thread(() -> {
            // SocketTimeoutException은 read가 blocking 상태일때 발생, 따라서 서버의 연결 소켓 read() 후에 connection timeout 시간이 지난뒤 클랑이언트 연결 소켓 write()
            assertThrows(SocketTimeoutException.class, () -> serverConnectedSocket.read(serverBuffer, 0, serverBuffer.length));
        });
        readThread.start();
        Thread.sleep(connectionTimeout); // 충분한 시간 대기하여 타임아웃 발생 유도
        clientConnectedSocket.write(clientBuffer, 0, clientBuffer.length); // 이후에 데이터 전송
    }
}