package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class SocketWrapperBase<E> {
    private static final Logger log = LoggerFactory.getLogger(SocketWrapperBase.class);
    protected final E socket;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;
    private int keepAliveCount = 0;

    // 추후 config -> endpoint로 변경
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

    public int getKeepAliveCount() { return keepAliveCount; }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
}
