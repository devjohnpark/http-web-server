package org.dochi.http.api.example;//package org.dochi;

import org.dochi.http.api.AbstractHttpApiHandler;
import org.dochi.http.data.HttpStatus;
import org.dochi.http.external.HttpExternalRequest;
import org.dochi.http.external.HttpExternalResponse;
import org.dochi.webresource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UploadFileHttpApiHandler extends AbstractHttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(UploadFileHttpApiHandler.class);

    @Override
    public void service(HttpExternalRequest request, HttpExternalResponse response) throws IOException {
        if (request.getMethod().equalsIgnoreCase("POST")) {
            doPost(request, response);
        } else {
            super.service(request, response);
        }
    }

    @Override
    public void doPost(HttpExternalRequest request, HttpExternalResponse response) throws IOException {
        if (request.getPart("username") != null && request.getPart("file") != null) {
            request.getPart("file").getContent();
            Resource resource = webResourceProvider.getResource("upload.html");
            if (!resource.isEmpty()) {
                response.send(HttpStatus.OK, resource.getData(), resource.getContentType(""));
            }
        } else {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
