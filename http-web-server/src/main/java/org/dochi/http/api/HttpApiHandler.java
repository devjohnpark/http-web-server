package org.dochi.http.api;

import org.dochi.http.request.HttpRequest;
import org.dochi.http.response.HttpResponse;
import org.dochi.webresource.WebResourceProvider;

import java.io.IOException;

public interface HttpApiHandler {
    void init(WebResourceProvider webResourceProvider);
    void handleApi(HttpRequest request, HttpResponse response) throws IOException;
}
