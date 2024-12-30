package org.dochi.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler1 implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler1.class);
    private final Socket connectedSocket;

    public RequestHandler1(Socket connectedSocket) {
        this.connectedSocket = connectedSocket;
    }

    @Override
    public void run() {
        log.debug("New Client Connect! Connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        try (
                InputStream in = connectedSocket.getInputStream();
                OutputStream out = connectedSocket.getOutputStream();
        ) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            log.debug("Request Line: {}", line);
//            if (line == null) {
//                Thread.sleep(10000);
//                return;
//            }
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello Client!".getBytes();
            responseHeader(dos, body.length);
            responseBody(dos, in, body);
        } catch (IOException e) {
            log.error("Error get socket i/o stream: " + e.getMessage());
        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void responseHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            // HTTP 메세지에서 문자열 줄끝을 구분하기 위해 '\r\n'을 사용
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes( "Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n"); // HTTP header 마지막줄에 body을 구분하기 위해 반드시 필요
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, InputStream in, byte[] body) {
        try {
            dos.write(body, 0, body.length);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
