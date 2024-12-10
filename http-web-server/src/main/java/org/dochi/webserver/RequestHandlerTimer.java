package org.dochi.webserver;

import org.dochi.http.api.HttpApiHandler;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.response.HttpResponse;
import org.dochi.webresource.Resource;
import org.dochi.webresource.WebResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RequestHandlerTimer extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandlerTimer.class);
    private final Socket connectedSocket;
    private final RequestMapper requestMapper;

    private static final int KEEP_ALIVE_TIMEOUT = 100000; // 10초 타임아웃
    private static final int MAX_KEEP_ALIVE_REQUESTS = 1000000000; // 최대 요청 수

    public RequestHandlerTimer(Socket connectedSocket, RequestMapper requestMapper) {
        this.connectedSocket = connectedSocket;
        this.requestMapper = requestMapper;
    }

    // 1. 소켓 읽기 타임 아웃 초과시 읽기 하면 예외 발생
    // 2. 그렇다면 소켓 읽기 타임 아웃 초과했다면 대기 큐에 스레드 남아있어서 리소스 낭비됨
    // 3. keep-alive timeout에 소켓 쓰기 시간이 반영안되어 있다.
    // 4. 응답할려다 timeout 초과 408 예외를 보내서 클라이언트가 다시 요청을 보낼수 있도록 한다.

    // 1. 스레드에 유효 시간을 부여
    // 2. 클라이언트 연결 소켓에 keep-alive 설정 (setSoTimeout으로 읽기 시간 설정)
    // 3. 연결 소켓 모니터링
    // 4. 직접 타이머 구현

    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        try (
                InputStream in = connectedSocket.getInputStream();
                OutputStream out = connectedSocket.getOutputStream();
        ) {

            boolean keepAlive = true;
            int requestCount = 0;
            long lastRequestTime = System.currentTimeMillis();

            while (keepAlive && requestCount < MAX_KEEP_ALIVE_REQUESTS) {
                log.debug("LOOP");
                // HTTP 요청 처리
                if (!processRequest(in, out)) {
                    keepAlive = false;
                }
                lastRequestTime = System.currentTimeMillis();
                requestCount++;

                if (System.currentTimeMillis() - lastRequestTime > KEEP_ALIVE_TIMEOUT) {
                    log.debug("요청 시간 초과: 연결 종료");
                    keepAlive = false;
                }
            }
        } catch (IOException e) {
            // 연결처리중 에러 발생
            log.error("연결 처리 중 에러: " + e.getMessage());
        }
    }

    private boolean processRequest(InputStream in, OutputStream out) {
        try {
            log.debug("processRequest");

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
            httpApiHandler.handleApi(request, response);

            if (request.getConnection().equals("close")) {
                log.error("Connection Header: close");
            }
            // Connection: close 헤더가 있는 경우 false 반환
            return !request.getConnection().equals("close");
        } catch (SocketTimeoutException e) {
            log.error("타임아웃 발생: 연결 종료");
            // Socket의 입출력 시간 설정인, setSoTimeout이 만료된 후에 입출력 수행하면 발생
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
            return false; // 타임아웃 발생 시 연결 종료
        } catch (IllegalStateException e) {
            log.error("Request Line is null");
            return false;
        } catch (IOException e) {
            // . 클라이언트가 연결 종료했을때, HttpRequest에서 소켓 버퍼에서 데이터를 읽어서 IOException 발생
            log.error(e.getMessage());
            return false;
        }
    }
}

