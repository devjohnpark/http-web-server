package org.dochi.inputbuffer.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


// 변경할 사항 1
// SocketTaskExecutor의 execute(Socket socket)는 Socket 객체에 종속
// SocketTaskExecutor의.execute(E socket)으로 변경

// 변경할 사항 2
// 서버 설정(WebServer)에서 setEndpoint -> Bio (ServerSocket, Socket으로 구성된 Endpoint)
// SocketTaskExecutorFactory에서 설정된 Endpoint로 SocketWrapperBase<?> 자식 클래스 생성 후 SocketTaskHandler에 주입

public class BioSocketWrapper extends SocketWrapperBase<Socket> {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapper.class);

    public BioSocketWrapper(Socket socket, KeepAlive config) throws SocketException {
        super(socket, config);
    }

    // 클라와 연결된 소켓 버퍼(TCP Buffer)에 데이터가 입력될때까지 blocking (즉, 클라이언트가 보낸 데이터가 보낼때까지 blocking 됨)
    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        return socket.getInputStream().read(buffer, off, len);
    }

    // 클라와 연결된 소켓 버퍼(TCP Buffer)에 데이터를 출력될때까지 blocking (즉, 소켓 버퍼가 꽉차서 못보낼때까지 blocking 됨)
    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        socket.getOutputStream().write(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        if (!socket.isClosed()) {
            socket.close();
            log.debug("Socket closed [Client: {}, Port: {}]",
                    socket.getInetAddress(), socket.getPort());
        }
    }

    @Override
    public void flush() throws IOException {
        socket.getOutputStream().flush();
    }

    @Override
    public boolean isConnected() { return !isClosed() && socket.isConnected(); }

    @Override
    public boolean isClosed() { return socket.isClosed(); }

    @Override
    public void startConnectionTimeout(int connectionTimeout) throws SocketException {
        socket.setSoTimeout(connectionTimeout);
        log.debug("Start connection timeout: {} [Client IP: {}]", connectionTimeout, socket.getInetAddress());
    }
}

