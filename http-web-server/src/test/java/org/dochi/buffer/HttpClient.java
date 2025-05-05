package org.dochi.buffer;

import org.dochi.inputbuffer.socket.SocketWrapperBase;

import java.io.IOException;

public class HttpClient {
    SocketWrapperBase<?> clientSocket;

    public HttpClient(SocketWrapperBase<?> clientSocketWrapper) {
        this.clientSocket = clientSocketWrapper;
    }

    public void doRequest(byte[] input) throws IOException {
        clientSocket.write(input, 0, input.length);
        clientSocket.flush();
    }
}