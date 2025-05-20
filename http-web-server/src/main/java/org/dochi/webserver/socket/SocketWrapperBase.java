package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// SocketWrapperBase 객체를 미리 생성해놓으면 안된다.
// 이전까지는 워커 스레드의 개수만큼 미리 SocketWrapperBase 객체를 생성하고 Socket을 setter로 주입하였다.
// 그리고 연결된 소켓의 개수와 워커 스레드의 개수가 다르다. 워커 스레드는 200개인데, 연결된 클라이언트는 2000개 일수도 있다.
// 일반적으로 Keep-Alive나 비동기 요청으로 커넥션 수 > 워커 수인 경우가 많다.
// 그러므로 Socket 객체가 없는 껍데기 SocketWrapperBase 객체를 생성하며 불필요한 메모리만 차지하게된다.
public abstract class SocketWrapperBase<E> {
    private static final Logger log = LoggerFactory.getLogger(SocketWrapperBase.class);
    protected final E socket;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;
    private int keepAliveCount = 0;

    // config -> endpoint로 변경해 소켓 버퍼 크기도 지정할수 있도록 향후 변경
    public SocketWrapperBase(E socket, KeepAlive config) {
        this.socket = socket;
        this.keepAliveTimeout = config.getKeepAliveTimeout();
        this.maxKeepAliveRequests = config.getMaxKeepAliveRequests();
    }

    public abstract int read(byte[] buffer, int off, int len) throws IOException;

    public abstract void write(byte[] buffer, int off, int len) throws IOException;

    public abstract void close() throws IOException;

    public abstract void flush() throws IOException;

    public abstract boolean isConnected();

    public abstract boolean isClosed();

    public abstract void startConnectionTimeout(int connectionTimeout) throws IOException;

    public abstract void setReceiveBufferSize(int receiveBufferSize) throws IOException;
    public abstract void setSendBufferSize(int sendBufferSize) throws IOException;

    public int incrementKeepAliveCount() { return ++keepAliveCount; }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getKeepAliveCount() { return keepAliveCount; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
}
