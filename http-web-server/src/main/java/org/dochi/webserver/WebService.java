package org.dochi.webserver;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.HttpApiHandler;
import org.dochi.webresource.WebResourceProvider;

import java.util.HashMap;
import java.util.Map;

public class WebService {
    private final Map<String, HttpApiHandler> services = new HashMap<>();
    private String webResourceBase = "webapp";

    public WebService() {
        services.put("/", new DefaultHttpApiHandler());
    }

    public WebService addService(String path, HttpApiHandler service) {
        services.put(path, service);
        return this;
    }

    public void setWebResourceBase(String webResourceBase) {
        this.webResourceBase = webResourceBase;
    }

    public Map<String, HttpApiHandler> getServices() {
        return initServices(new WebResourceProvider(webResourceBase));
    }

    private Map<String, HttpApiHandler> initServices(WebResourceProvider webResourceProvider) {
        for (HttpApiHandler service : services.values()) {
            service.init(webResourceProvider);
        }
        return services;
    }
}
