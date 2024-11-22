package org.doci.http.api;

import org.doci.http.request.HttpMethod;
import org.doci.http.request.HttpRequest;
import org.doci.http.request.HttpVersion;
import org.doci.http.response.HttpResponse;
import org.doci.http.response.HttpStatus;
import org.doci.webresource.WebResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpApiHandler implements HttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpApiHandler.class);

    public WebResourceProvider webResourceProvider = null;

    @Override
    public void init(WebResourceProvider webResourceProvider) {
        this.webResourceProvider = webResourceProvider;
    }

    @Override
    public void handleApi(HttpRequest request, HttpResponse response) {
        HttpMethod method = request.getMethod();
        if (HttpMethod.GET.equals(method)) {
            doGet(request, response);
        } else if (HttpMethod.POST.equals(method)) {
            doPost(request, response);
        } else if (HttpMethod.PUT.equals(method)) {
            doPut(request, response);
        }  else if (HttpMethod.PATCH.equals(method)) {
            doPatch(request, response);
        } else if (HttpMethod.DELETE.equals(method)) {
            doDelete(request, response);
        } else {
            response.sendError(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) { sendDefaultError(request, response); }

    protected void doPost(HttpRequest request, HttpResponse response) { sendDefaultError(request, response); }

    protected void doPut(HttpRequest request, HttpResponse response) {
        sendDefaultError(request, response);
    }

    protected void doPatch(HttpRequest request, HttpResponse response) {
        sendDefaultError(request, response);
    }

    protected void doDelete(HttpRequest request, HttpResponse response) {
        sendDefaultError(request, response);
    }

    private void sendDefaultError(HttpRequest request, HttpResponse response) {
        HttpVersion protocol = request.getHttpVersion();
        if (protocol.equals(HttpVersion.HTTP_0_9) || protocol.equals(HttpVersion.HTTP_1_0)) {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED);
        } else {
            response.sendError(HttpStatus.BAD_REQUEST);
        }
    }
}
