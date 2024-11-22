package org.doci.webserver;

import org.doci.http.api.AbstractHttpApiHandler;
import org.doci.http.api.DefaultHttpApiHandler;
import org.doci.webresource.WebResourceProvider;

import java.util.HashMap;
import java.util.Map;

public class WebService {
    private Map<String, AbstractHttpApiHandler> services = new HashMap<>();
    private String webResourceBase = "webapp";

    public void addService(String path, AbstractHttpApiHandler service) {
        services.put(path, service);
    }

    public void setServices(Map<String, AbstractHttpApiHandler> services) {
        this.services = new HashMap<>(services);
    }

    public void setWebResourceBase(String webResourceBase) {
        this.webResourceBase = webResourceBase;
    }

    public Map<String, AbstractHttpApiHandler> getServices() {
        inputDefaultService(webResourceBase);
        initServices(new WebResourceProvider(webResourceBase));
        return services;
    }

    private void initServices(WebResourceProvider webResourceProvider) {
        for (AbstractHttpApiHandler service : services.values()) {
            service.init(webResourceProvider);
        }
    }

    private void inputDefaultService(String webBase) {
        if (webResourceBase.equals("webapp")) {
            services.put("/", new DefaultHttpApiHandler());
        }
    }
}
