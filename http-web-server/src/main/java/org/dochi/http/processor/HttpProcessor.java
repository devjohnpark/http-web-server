//package org.dochi.http.processor;
//
//import org.dochi.http.api.HttpApiHandler;
//import org.dochi.http.request.HttpRequest;
//import org.dochi.http.response.HttpResponse;
//import org.dochi.webserver.RequestHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class HttpProcessor {
//    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);
//    private static final int KEEP_ALIVE_TIMEOUT = 1000; // 1초 타임아웃
//    private static final int MAX_KEEP_ALIVE_REQUESTS = 100000; // 최대 요청 수
//
//    //
//    public void service(HttpRequest request, HttpResponse response) {
//        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
//
//        try (
//                InputStream in = connectedSocket.getInputStream();
//                OutputStream out = connectedSocket.getOutputStream();
//        ) {
//            HttpRequest request = new HttpRequest(in);
//            HttpResponse response = new HttpResponse(out);
//            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
//            httpApiHandler.handleApi(request, response);
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//
//    }
//}
