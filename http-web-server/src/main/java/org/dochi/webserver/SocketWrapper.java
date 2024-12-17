package org.dochi.webserver;

import org.dochi.webserver.config.KeepAlive;

import java.net.Socket;

public class SocketWrapper {
    private Socket socket = null;
    private final KeepAlive keepAlive;

    public SocketWrapper(KeepAlive keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setSocket(Socket socket) { this.socket = socket; }

    public Socket getSocket() { return socket; }

    public KeepAlive getKeepAlive() { return keepAlive; }
}
