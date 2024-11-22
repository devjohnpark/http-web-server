package org.doci.webserver;

import org.doci.http.api.AbstractHttpApiHandler;
import org.doci.http.api.DefaultHttpApiHandler;
import org.doci.http.api.LoginHttpApiHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class WebServiceTest {
    private WebService webService;

    @BeforeEach
    void setUp() {
        webService = new WebService();
    }

    @Test
    void getService_default() {
        assertEquals(DefaultHttpApiHandler.class, webService.getServices().get("/").getClass());
    }

    @Test
    void addService() {
        webService.addService("/user", new LoginHttpApiHandler());
        assertEquals(LoginHttpApiHandler.class, webService.getServices().get("/user").getClass());
    }
}