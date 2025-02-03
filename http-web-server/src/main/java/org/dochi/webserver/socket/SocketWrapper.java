package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketException;

// Socket 객체를 변수로 받아서 스레드 재활용
public class SocketWrapper {
    private static final Logger log = LoggerFactory.getLogger(SocketWrapper.class);
    private Socket socket = null;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;
    private int keepAliveCount = 0;
    private SocketState socketState;

    public SocketWrapper(KeepAlive keepAlive) {
        this.keepAliveTimeout = keepAlive.getKeepAliveTimeout();
        this.maxKeepAliveRequests = keepAlive.getMaxKeepAliveRequests();
    }

    public void setConnectedSocket(Socket socket) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket is null");
        }
        if (!isConnected(socket)) {
            throw new IllegalArgumentException("Socket is not connected");
        }
        this.socket = socket;
        this.keepAliveCount = 0;
        socketState = SocketState.OPENING;
    }

    public Socket getSocket() {
        if (socket == null) {
            throw new IllegalStateException("Socket is not set");
        }
        return socket;
    }

    private boolean isConnected(Socket socket) {
        return socket.isConnected() && !socket.isClosed();
    }

    public boolean isUsing() {
        return socket != null && !socket.isClosed();
    }

    public void startConnectionTimeout(int connectionTimeout) throws SocketException {
        getSocket().setSoTimeout(connectionTimeout);
        log.debug("Start connection timeout: {} [Client IP: {}]", connectionTimeout, getSocket().getInetAddress());
    }

    public int incrementKeepAliveCount() {
        return ++keepAliveCount;
    }

    public int getKeepAliveCount() {
        return keepAliveCount;
    }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }

    public void markUpgrading() {
        validateSocketState();
        this.socketState = SocketState.UPGRADING;
    }

    public void markClosing() {
        validateSocketState();
        this.socketState = SocketState.CLOSING;
    }

    private void validateSocketState() {
        if (socketState != SocketState.OPENING) {
            throw new IllegalStateException("Socket is not open");
        }
    }

    public boolean isUpgrading() {
        return socketState == SocketState.UPGRADING && isConnected(socket);
    }

    public boolean isClosing() {
        return socketState == SocketState.CLOSING;
    }

    private enum SocketState {
        OPENING,
        CLOSING,
        UPGRADING,
    }
}
