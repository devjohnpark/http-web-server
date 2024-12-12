package org.dochi.webserver;

import java.net.Socket;

public class SocketWrapper {
//    private int keepAliveTimeout = 10000; // -1: unlimited
//    private int maxKeepAliveRequests = 10000; // -1: unlimited
    private Socket socket = null;
    private KeepAlive keepAlive = null;

    public SocketWrapper(Socket socket, KeepAlive keepAlive) {
        this.socket = socket;
        this.keepAlive = keepAlive;
    }

    public void changeSocket(Socket socket) { this.socket = socket; }

    public Socket getSocket() { return socket; }

    public KeepAlive getKeepAlive() { return keepAlive; }

//    public int getKeepAliveTimeout() { return keepAliveTimeout; }
//
//    public int getMaxKeepAliveRequests() { return maxKeepAliveRequests; }
//
//    public void setKeepAliveTimeout(int keepAliveTimeout) {
//        if (keepAliveTimeout < 0) {
//            throw new IllegalArgumentException("keepAliveTimeout cannot be negative");
//        }
//        this.keepAliveTimeout = keepAliveTimeout;
//    }
//
//    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
//        if (maxKeepAliveRequests < 0) {
//            throw new IllegalArgumentException("maxKeepAliveRequests cannot be negative");
//        }
//        this.maxKeepAliveRequests = maxKeepAliveRequests;
//    }
}
