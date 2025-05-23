package org.dochi.webserver.protocol;

import org.dochi.processor.HttpProcessor;
import org.dochi.webserver.config.HttpConfig;
import org.dochi.webserver.config.HttpReqConfig;
import org.dochi.webserver.config.HttpResConfig;
import org.dochi.webserver.config.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpProtocolHandlerTest {
    HttpProtocolHandler handler;
    ServerConfig config = new ServerConfig();

    @BeforeEach
    void init() {
        handler = new HttpProtocolHandler(new HttpConfig(
                new HttpReqConfig(config.getHttpReqAttribute()),
                new HttpResConfig(config.getHttpResAttribute())
        ), config.getHttpProcessor());
    }

    @Test
    void getProcessor() {
        HttpProcessor processor = handler.getProcessor();
        assertEquals(handler.getSize() + 1, config.getHttpProcessor().getPoolSize());
    }

    @Test
    void release() {
        HttpProcessor processor = handler.getProcessor();
        assertEquals(handler.getSize() + 1, config.getHttpProcessor().getPoolSize());
        handler.release(processor);
        assertEquals(handler.getSize(), config.getHttpProcessor().getPoolSize());
    }

    @Test
    void getSize() {
        assertEquals(handler.getSize(), config.getHttpProcessor().getPoolSize());
    }
}