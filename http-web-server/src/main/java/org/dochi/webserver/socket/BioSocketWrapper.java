package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


// 변경할 사항 1
// SocketTaskExecutor의 execute(Socket socket)는 Socket 객체에 종속되어 있다.
// SocketTaskExecutor의.execute(E socket)으로 변경.

// 변경할 사항 2
// 서버 설정(WebServer)에서 setEndpoint -> Bio (ServerSocket, Socket으로 구성된 Endpoint)
// SocketTaskExecutorFactory에서 설정된 Endpoint로 SocketWrapperBase<?> 자식 클래스 생성 후 SocketTaskHandler에 주입
public class BioSocketWrapper extends SocketWrapperBase<Socket> {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapper.class);

    public BioSocketWrapper(KeepAlive config) {
        super(config);
    }

    // 클라와 연결된 소켓 버퍼(TCP Buffer)에 데이터가 입략될때까지 blocking (즉, 클라이언트가 보낸 데이터가 보낼때까지 blocking 됨)
    @Override
    protected int read(byte[] buffer, int off, int len) throws IOException {
        return getSocket().getInputStream().read(buffer, off, len);
    }

    // 클라와 연결된 소켓 버퍼(TCP Buffer)에 데이터를 출력될때까지 blocking (즉, 소켓 버퍼가 꽉차서 못보낼때까지 blocking 됨)
    @Override
    protected void write(byte[] buffer, int off, int len) throws IOException {
        getSocket().getOutputStream().write(buffer, off, len);
    }

    @Override
    protected void close() throws IOException {
        if (!getSocket().isClosed()) {
            getSocket().close();
            log.debug("Socket closed [Client: {}, Port: {}]",
                    getSocket().getInetAddress(), getSocket().getPort());
        }
    }

    @Override
    protected boolean isConnected(Socket socket) {
        return !isClosed(socket) && socket.isConnected();
    }

    @Override
    protected boolean isClosed(Socket socket) {
        return socket.isClosed();
    }

    @Override
    protected void startConnectionTimeout(int connectionTimeout) throws SocketException {
        getSocket().setSoTimeout(connectionTimeout);
    }
}
