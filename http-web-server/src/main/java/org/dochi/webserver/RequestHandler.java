package org.dochi.webserver;

import org.dochi.http.http1.HttpProcessor;
import org.dochi.http.request.HttpRequest;
import org.dochi.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RequestHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final SocketWrapper socketWrapper;
    private final RequestMapper requestMapper;

    // Socket 객체를 변수로 받아서 스레드 재활용
    public RequestHandler(SocketWrapper socketWrapper, RequestMapper requestMapper) {
        this.socketWrapper = socketWrapper;
        this.requestMapper = requestMapper;
    }

    // 1. 소켓 읽기 타임 아웃 초과시 읽기나 쓰기 하면 예외 발생
    // 2. 그렇다면 소켓 읽기 타임 아웃 초과했다면 대기 큐에 스레드 남아있어서 리소스 낭비됨
    // 4. 요청을 처리할려다 timeout 초과 408 예외를 보내서 클라이언트가 다시 요청을 보낼수 있도록 한다.

    // 1. 스레드에 유효 시간을 부여
    // 2. 클라이언트 연결 소켓에 setSoTimeout으로 읽기 시간 설정 -> 연결 소켓 모니터링 -> getSoTimeout 호출 0이면 제거
    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", socketWrapper.getSocket().getInetAddress(), socketWrapper.getSocket().getPort());
        try (
                InputStream in = socketWrapper.getSocket().getInputStream();
                OutputStream out = socketWrapper.getSocket().getOutputStream();
             )
        {
            HttpProcessor httpProcessor = new HttpProcessor(new HttpRequest(in), new HttpResponse(out));
            httpProcessor.process(socketWrapper, requestMapper);
        }  catch (IOException e) {
            log.error("Error get socket i/o stream: {}", e.getMessage());
        }
    }

    public SocketWrapper getSocketWrapper() {
        return socketWrapper;
    }
}
