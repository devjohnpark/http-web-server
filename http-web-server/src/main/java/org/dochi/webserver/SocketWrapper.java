package org.dochi.webserver;

import org.dochi.webserver.config.KeepAlive;

import java.net.Socket;
import java.net.SocketException;

public class SocketWrapper {
    private Socket socket = null;
    private final int keepAliveTimeout;
    private final int maxKeepAliveRequests;

    public SocketWrapper(int keepAliveTimeout, int maxKeepAliveRequests) {
        this.keepAliveTimeout = keepAliveTimeout;
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }

    public void setSocket(Socket socket) { this.socket = socket; }

    public Socket getSocket() {
        if (socket == null) {
            throw new IllegalStateException("Socket not set");
        }
        return socket;
    }

    public boolean isUsing() { return socket != null && !socket.isClosed(); }

    public int getKeepAliveTimeout() { return keepAliveTimeout; }

    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
}
