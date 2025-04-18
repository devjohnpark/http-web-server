package org.dochi.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;

public abstract class SocketWrapperBase<E> {
    private static final Logger log = LoggerFactory.getLogger(SocketWrapperBase.class);
    protected final E socket;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;
    private int keepAliveCount = 0;

    // config -> endpoint로 변경해 소켓 버퍼 크기도 지정할수 있도록 향후 변경
    public SocketWrapperBase(E socket, SocketConfig config) {
        this.socket = socket;
        this.keepAliveTimeout = config.getKeepAliveTimeout();
        this.maxKeepAliveRequests = config.getMaxKeepAliveRequests();
    }

    public abstract int read(byte[] buffer, int off, int len) throws IOException;

    public abstract void write(byte[] buffer, int off, int len) throws IOException;

    public abstract void close() throws IOException;

    public abstract void flush() throws IOException;

    // SocketWrapper를 HttpProcessor로 넘겼을때 getSocket() 메서드를 넘기면 실제 소켓객체가 넘겨지기 때문에 문제가된다.
    // 따라서 Socket의 set, get 로직을 제거하고 소켓을 초기화해라.
//    protected E getSocket() {
//        if (socket == null) {
//            throw new IllegalStateException("Socket is not set");
//        }
//        return socket;
//    }

    protected abstract boolean isConnected();

    protected abstract boolean isClosed();

    protected abstract void startConnectionTimeout(int connectionTimeout) throws SocketException;

    public int incrementKeepAliveCount() { return ++keepAliveCount; }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getKeepAliveCount() { return keepAliveCount; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

}
