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
    void addService() {
        webService.addService("/user", new LoginHttpApiHandler());
        assertEquals(LoginHttpApiHandler.class, webService.getServices().get("/user").getClass());
    }

    @Test
    void setServices() {
        Map<String, AbstractHttpApiHandler> webServices = Map.of(
                "/", new DefaultHttpApiHandler(),
                "/user", new LoginHttpApiHandler()
        );

        webService.setServices(webServices);
        assertEquals(DefaultHttpApiHandler.class, webService.getServices().get("/").getClass());
        assertEquals(LoginHttpApiHandler.class, webService.getServices().get("/user").getClass());
    }
}