package org.dochi.webserver.context;

import org.dochi.http.api.HttpApiHandler;
import org.dochi.webresource.WebResourceProvider;
import org.dochi.webserver.lifecycle.Lifecycle;
import org.dochi.webserver.config.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WebServiceContext implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(WebServiceContext.class);
    private final WebService webService;

    public WebServiceContext(WebService webService) {
        this.webService = webService;
    }

    @Override
    public void start() {
        // Noting by default
    }

    @Override
    public void stop() {
        // Noting by default
    }

    @Override
    public void init() {
        Map<String, HttpApiHandler> services = webService.getServices();
        WebResourceProvider webResourceProvider = new WebResourceProvider(webService.getWebResourceBase());
        for (HttpApiHandler service : services.values()) {
            service.init(webResourceProvider);
        }
    }

    @Override
    public void destroy() {
        Map<String, HttpApiHandler> services = webService.getServices();
        for (HttpApiHandler service : services.values()) {
            service.destroy();
        }
    }
}
