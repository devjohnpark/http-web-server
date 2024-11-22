package org.doci.http.api;

import org.doci.http.request.HttpRequest;
import org.doci.http.response.HttpResponse;
import org.doci.webresource.WebResourceProvider;

public interface HttpApiHandler {
    void init(WebResourceProvider webResourceProvider);
    void handleApi(HttpRequest request, HttpResponse response);
}
