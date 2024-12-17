package org.dochi.webserver.config;

import org.dochi.http.api.DefaultHttpApiHandler;
import org.dochi.http.api.HttpApiHandler;

import java.util.HashMap;
import java.util.Map;

// StandardWrapper extends ContainerBase

// WebServiceWrapper or WebServiceContainer extends ContainerBase
// interface Container extends Lifecycle
// Lifecycle { init(), start(), stop(), destroy() }


// StandardWrapper.stopInternal() -> StandardWrapper.unload() -> Servlet.destroy();

// WebServiceContext -> stop

// Tomcat.start() -> Server.start() -> LifecycleBase implements Lifecycle.start()
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

//    // 아래서부터는 사용자가 호출하면 안된다.
//    public Map<String, HttpApiHandler> getServices() {
//        WebResourceProvider webResourceProvider = new WebResourceProvider(webResourceBase);
//        return initServices(webResourceProvider);
//    }
//
//    private Map<String, HttpApiHandler> initServices(WebResourceProvider webResourceProvider) {
//        for (HttpApiHandler service : services.values()) {
//            service.init(webResourceProvider);
//        }
//        return services;
//    }

//    public void destroyServices() {
//        for (HttpApiHandler service : services.values()) {
//            service.destroy();
//        }
//    }
}
