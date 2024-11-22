package org.doci.webserver;

import org.doci.http.api.AbstractHttpApiHandler;
import org.doci.http.api.DefaultHttpApiHandler;
import org.doci.http.api.LoginHttpApiHandler;
import org.doci.webresource.WebResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {
    private RequestMapper requestMapper;
    private final WebResourceProvider webResourceProvider = new WebResourceProvider("webbapp");

    @BeforeEach
    void setUp() {

        Map<String, AbstractHttpApiHandler> requestMappings = Map.of(
                "/", new DefaultHttpApiHandler(),
                "/user/create", new LoginHttpApiHandler()
        );
        requestMapper = new RequestMapper(requestMappings);
    }

    @Test
    void get_httpApiHandler_default() {
        assertEquals(DefaultHttpApiHandler.class, requestMapper.getHttpApiHandler("/").getClass());
        assertEquals(LoginHttpApiHandler.class, requestMapper.getHttpApiHandler("/user/create").getClass());
    }

    @Test
    void get_not_find_httpApiHandler() {
        assertEquals(DefaultHttpApiHandler.class, requestMapper.getHttpApiHandler("/post").getClass());
    }
}