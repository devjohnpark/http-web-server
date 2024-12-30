package org.dochi.webserver.config;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.LoginHttpApiHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebServiceTest {

    private WebService webService;

    @BeforeEach
    void setUp() {
        webService = new WebService();
    }

    @Test
    void setWebResourceBase() {
        webService.setWebResourceBase("webapp2");
        assertEquals("webapp2", webService.getWebResourceBase());
    }

    @Test
    void getWebResourceBase() {
        assertEquals("webapp", webService.getWebResourceBase());
    }

    @Test
    void addService() {
        webService.addService("/user", new LoginHttpApiHandler());
        assertEquals(LoginHttpApiHandler.class, webService.getServices().get("/user").getClass());
    }

    @Test
    void getServices() {
        assertEquals(DefaultHttpApiHandler.class, webService.getServices().get("/").getClass());
    }
}