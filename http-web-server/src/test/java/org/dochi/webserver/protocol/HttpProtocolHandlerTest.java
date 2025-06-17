package org.dochi.webserver.protocol;

import org.dochi.http.internal.processor.HttpProcessor;
import org.dochi.webserver.config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpProtocolHandlerTest {
    HttpProtocolHandler protocolHandler;
    ServerConfig serverConfig = new ServerConfig();
    HttpConfig httpConfig = new HttpConfigImpl(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());


    @BeforeEach
    void init() {
        protocolHandler = new HttpProtocolHandler(httpConfig);
    }

    @Test
    void getProcessor() {
        HttpProcessor processor = protocolHandler.getProcessor();
        assertEquals(0, protocolHandler.getSize());
    }

    @Test
    void release() {
        HttpProcessor processor = protocolHandler.getProcessor(); // 0
        protocolHandler.release(processor); // 1
        processor = protocolHandler.getProcessor(); // 0
        protocolHandler.release(processor); // 1
        assertEquals(1, protocolHandler.getSize());
    }

    @Test
    void getSize() {
        assertEquals(protocolHandler.getSize(), 0);
    }
}