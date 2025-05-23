package org.dochi.webserver;

import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;

public class HttpClient {
    private SocketWrapperBase<?> clientSocket;

    public HttpClient(SocketWrapperBase<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}