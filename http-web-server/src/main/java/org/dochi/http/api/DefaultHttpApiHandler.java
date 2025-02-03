package org.dochi.http.api;

import org.dochi.http.request.data.HttpMethod;
import org.dochi.http.response.Http11ResponseProcessor;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultHttpApiHandler extends AbstractHttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultHttpApiHandler.class);

    @Override
    public void service(HttpApiRequest request, Http11ResponseProcessor response) throws IOException {
        if (HttpMethod.GET.equals(request.getMethod())) {
            doGet(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    public void doGet(HttpApiRequest request, Http11ResponseProcessor response) throws IOException {
        Resource resource = webResourceProvider.getResource(request.getPath());
        if (resource.isEmpty()) {
            response.sendError(HttpStatus.NOT_FOUND);
        } else {
            response.send(HttpStatus.OK, resource.getData(), resource.getContentType("UTF-8"));
        }
    }
}
