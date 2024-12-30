package org.dochi.webserver.config;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.HttpApiHandler;

import java.util.HashMap;
import java.util.Map;

public class WebService {
    private final Map<String, HttpApiHandler> services = new HashMap<>();
    private String webResourceBase = "webapp";

    public WebService() {
        services.put("/", new DefaultHttpApiHandler());
    }

    public void setWebResourceBase(String webResourceBase) {
        this.webResourceBase = webResourceBase;
    }

    public String getWebResourceBase() {
       return webResourceBase;
    }

    public WebService addService(String path, HttpApiHandler service) {
        services.put(path, service);
        return this;
    }

    public Map<String, HttpApiHandler> getServices() {
        return services;
    }
}
