package org.dochi.webserver;

import org.dochi.webserver.config.KeepAlive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;


import java.net.Socket;

class SocketWrapperTest {
    private SocketWrapper socketWrapper;

    @BeforeEach
    void setUp() {
        socketWrapper = new SocketWrapper(new KeepAlive());
    }

    @Test
    void setSocket() {
        socketWrapper.setSocket(new Socket());
        assertNotNull(socketWrapper.getSocket());
    }

    @Test
    void getSocket() {
        assertNull(socketWrapper.getSocket());
    }

    @Test
    void getKeepAlive() {
        assertNotNull(socketWrapper.getKeepAlive());
    }
}