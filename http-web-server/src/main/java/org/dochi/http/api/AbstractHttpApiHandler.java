package org.dochi.http.api;

import org.dochi.http.request.HttpMethod;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.request.HttpVersion;
import org.dochi.http.response.HttpResponse;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.WebResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractHttpApiHandler implements HttpApiHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractHttpApiHandler.class);

    public WebResourceProvider webResourceProvider = null;

    @Override
    public void init(WebResourceProvider webResourceProvider) {
        this.webResourceProvider = webResourceProvider;
    }

    @Override
    public void destroy() {
        // Noting by default
    }

    @Override
    public void handleApi(HttpRequest request, HttpResponse response) throws IOException {
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

    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPut(HttpRequest request, HttpResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doPatch(HttpRequest request, HttpResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    protected void doDelete(HttpRequest request, HttpResponse response) throws IOException {
        sendDefaultError(request, response);
    }

    private void sendDefaultError(HttpRequest request, HttpResponse response) throws IOException {
        HttpVersion protocol = request.getHttpVersion();
        // PUT, PATCH, DELETE, OPTIONS 등은 HTTP/0.9나 HTTP/1.0에서 명시적으로 정의되어 있지 않는다.
        if (protocol.equals(HttpVersion.HTTP_0_9) || protocol.equals(HttpVersion.HTTP_1_0)) {
            response.sendError(HttpStatus.BAD_REQUEST);
        } else {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }
}
