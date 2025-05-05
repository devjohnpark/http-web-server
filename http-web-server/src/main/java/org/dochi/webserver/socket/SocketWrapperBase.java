//package org.dochi.webserver.socket;
//
//import org.dochi.webserver.attribute.KeepAlive;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.SocketException;
//
//public abstract class SocketWrapperBase<E> {
//    private static final Logger log = LoggerFactory.getLogger(SocketWrapperBase.class);
//    private E socket = null;
//    private final int connectionTimeout;
//    private final int maxKeepAliveRequests;
//    private volatile int keepAliveCount = 0;
//
//    public SocketWrapperBase(KeepAlive config) {
//        this.connectionTimeout = config.getKeepAliveTimeout();
//        this.maxKeepAliveRequests = config.getMaxKeepAliveRequests();
//    }
//
//    public void setConnectedSocket(E socket) {
//        if (socket == null) {
//            throw new IllegalArgumentException("Socket is null");
//        }
//        if (!isConnected(socket)) {
//            throw new IllegalArgumentException("Socket is not connected");
//        }
//        this.socket = socket;
//        this.keepAliveCount = 0;
//    }
//
//    protected abstract int read(byte[] buffer, int off, int len) throws IOException;
//
//    protected abstract void write(byte[] buffer, int off, int len) throws IOException;
//
//    protected abstract void close() throws IOException;
//
//    protected E getSocket() {
//        if (socket == null) {
//            throw new IllegalStateException("Socket is not set");
//        }
//        return socket;
//    }
//
//    protected abstract boolean isConnected(E socket);
//
//    protected abstract boolean isClosed(E socket);
//
////    public boolean isUsing() { return socket != null && !isClosed(); }
//
//    abstract void startConnectionTimeout(int connectionTimeout) throws SocketException;
//
//    public int incrementKeepAliveCount() { return ++keepAliveCount; }
//
//    public int getConnectionTimeout() { return connectionTimeout; }
//
//    public int getKeepAliveCount() { return keepAliveCount; }
//
//    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
//}
