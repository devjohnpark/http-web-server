package org.dochi.webserver.protocol;

import org.dochi.processor.HttpProcessor;
import org.dochi.webserver.config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpProtocolHandlerTest {
    HttpProtocolHandler protocolHandler;
    ServerConfig serverConfig = new ServerConfig();
    HttpConfig httpConfig = new HttpReqResConfig(serverConfig.getHttpReqAttribute(), serverConfig.getHttpResAttribute());


    @BeforeEach
    void init() {
        protocolHandler = new HttpProtocolHandler(httpConfig, serverConfig.getHttpProcessorAttribute());
    }

    @Test
    void getProcessor() {
        HttpProcessor processor = protocolHandler.getProcessor();
        assertEquals(protocolHandler.getSize() + 1, serverConfig.getHttpProcessorAttribute().getPoolSize());
    }

    @Test
    void release() {
        HttpProcessor processor = protocolHandler.getProcessor();
        assertEquals(protocolHandler.getSize() + 1, serverConfig.getHttpProcessorAttribute().getPoolSize());
        protocolHandler.release(processor);
        assertEquals(protocolHandler.getSize(), serverConfig.getHttpProcessorAttribute().getPoolSize());
    }

    @Test
    void getSize() {
        assertEquals(protocolHandler.getSize(), serverConfig.getHttpProcessorAttribute().getPoolSize());
    }
}