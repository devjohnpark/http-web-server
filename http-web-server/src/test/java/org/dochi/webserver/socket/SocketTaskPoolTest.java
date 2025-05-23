package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.attribute.HttpResAttribute;
import org.dochi.webserver.config.HttpAttribute;
import org.dochi.webserver.config.HttpReqConfig;
import org.dochi.webserver.config.HttpResConfig;
import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SocketTaskPoolTest {
    SocketTaskPool pool;
    ServerConfig serverConfig = new ServerConfig();


    HttpProtocolHandler protocolHandler = new HttpProtocolHandler(new HttpAttribute(new HttpReqConfig(new HttpReqAttribute()), new HttpResConfig(new HttpResAttribute())))

    @BeforeEach
    void setUp() {
        serverConfig.get
        this.pool = new SocketTaskPool(
                serverConfig.getThreadPool(),
                () -> new SocketTaskHandler(
                        protocolHandler,
                        httpApiMapper
                )
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void recycle() {
    }

    @Test
    void get() {
    }
}