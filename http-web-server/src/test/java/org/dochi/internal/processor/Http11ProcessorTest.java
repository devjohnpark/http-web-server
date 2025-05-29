package org.dochi.internal.processor;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.HttpClient;
import org.dochi.webserver.attribute.HttpReqAttribute;
import org.dochi.webserver.attribute.HttpResAttribute;
import org.dochi.webserver.config.HttpReqResConfig;
import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.socket.BioSocketWrapperConnectionTest;
import org.dochi.webserver.socket.SocketState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Http11ProcessorTest extends BioSocketWrapperConnectionTest {
    Http11Processor processor;
    ServerConfig serverConfig = new ServerConfig();
    HttpApiMapper apiMapper = new HttpApiMapper(serverConfig.getWebService());
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        processor = new Http11Processor(new HttpReqResConfig(new HttpReqAttribute(), new HttpResAttribute()));
        httpClient = new HttpClient(clientConnectedSocket);
    }

    @Test
    void process() throws IOException {
        httpClient.doRequest("GET / HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
        SocketState state = processor.process(serverConnectedSocket, apiMapper);
        assertEquals(SocketState.CLOSED, state);
    }
}