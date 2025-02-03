package org.dochi.webserver.config;

import org.dochi.webresource.WebResourceProvider;

public class WebServiceConfig {
    private final WebResourceProvider webResourceProvider;

    public WebServiceConfig(WebResourceProvider webResourceProvider) {
        this.webResourceProvider = webResourceProvider;
    }

    public WebResourceProvider getWebResourceProvider() {
        return webResourceProvider;
    }
}
