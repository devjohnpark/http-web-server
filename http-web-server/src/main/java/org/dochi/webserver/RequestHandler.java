package org.dochi.webserver;

import org.dochi.http.api.HttpApiHandler;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.request.HttpVersion;
import org.dochi.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connectedSocket;
    private final RequestMapper requestMapper;

    public RequestHandler(Socket connectedSocket, RequestMapper requestMapper) {
        this.connectedSocket = connectedSocket;
        this.requestMapper = requestMapper;
    }

    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        try (
                InputStream in = connectedSocket.getInputStream();
                OutputStream out = connectedSocket.getOutputStream();
        ) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            response.addVersion(request.getHttpVersion());
            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
            httpApiHandler.handleApi(request, response);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}