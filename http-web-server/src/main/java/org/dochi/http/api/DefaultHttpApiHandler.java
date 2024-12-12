package org.dochi.http.api;

import org.dochi.http.request.HttpMethod;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.response.HttpResponse;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultHttpApiHandler extends AbstractHttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultHttpApiHandler.class);

    @Override
    public void handleApi(HttpRequest request, HttpResponse response) throws IOException {
        if (HttpMethod.GET.equals(request.getMethod())) {
            doGet(request, response);
        } else {
            super.handleApi(request, response);
        }
    }

    @Override
    public void doGet(HttpRequest request, HttpResponse response) throws IOException {
        Resource resource = webResourceProvider.getResource(request.getPath());
        if (resource.isEmpty()) {
            response.sendError(HttpStatus.NOT_FOUND);
        } else {
            response.send(HttpStatus.OK, resource.getData(), resource.getContentType());
        }
    }
}
