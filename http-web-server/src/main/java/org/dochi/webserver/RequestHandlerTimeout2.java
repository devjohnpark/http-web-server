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
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class RequestHandlerTimeout2 extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandlerTimeout2.class);
    private final Socket connectedSocket;
    private final RequestMapper requestMapper;

    private static final int KEEP_ALIVE_TIMEOUT = 10000; // 3초 타임아웃
    private static final int MAX_KEEP_ALIVE_REQUESTS = 100000; // 최대 요청 수

    // Socket 객체를 변수로 받아서 스레드 재활용
    public RequestHandlerTimeout2(Socket connectedSocket, RequestMapper requestMapper) {
        this.connectedSocket = connectedSocket;
        this.requestMapper = requestMapper;
    }

    // 1. 소켓 읽기 타임 아웃 초과시 읽기나 쓰기 하면 예외 발생
    // 2. 그렇다면 소켓 읽기 타임 아웃 초과했다면 대기 큐에 스레드 남아있어서 리소스 낭비됨
    // 4. 요청을 처리할려다 timeout 초과 408 예외를 보내서 클라이언트가 다시 요청을 보낼수 있도록 한다.

    // 1. 스레드에 유효 시간을 부여
    // 2. 클라이언트 연결 소켓에 setSoTimeout으로 읽기 시간 설정 -> 연결 소켓 모니터링 -> getSoTimeout 호출 0이면 제거
    //  1) 스레드 풀에서 스레드 제거?
    //  2) Socket 객체를 체크하는 객체를 주입해서, 스레드 풀에서 스레드를 가져와서 실행시키기전에, 스레드에 Socket 객체 바꿔치기?
    //  3)
    // 3. 직접 시간을 측정한다.

    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        int requestCount = 0;
        try {
            // 소켓 읽기 타임아웃 설정
            connectedSocket.setSoTimeout(KEEP_ALIVE_TIMEOUT);
            InputStream in = connectedSocket.getInputStream();
            OutputStream out = connectedSocket.getOutputStream();


            while (requestCount < MAX_KEEP_ALIVE_REQUESTS) {
                log.debug("LOOP");
                // HTTP 요청 처리
                if (!processRequest(in, out)) {
                    break;
                }
                requestCount++;
            }
        } catch (SocketTimeoutException e) { // setSoTimeout throw SocketTimeoutException
            log.error("타임아웃 발생: 연결 종료");
            // Socket의 입출력 시간 설정인, read()나 write() 시스템 콜 추상화 메서드에 blocking 중인 상태에서 setSoTimeout이 만료되면 SocketTimeoutException 예외 던짐
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
        } catch (IOException e) {
            // 연결처리중 에러 발생
            log.error("연결 처리 중 에러: " + e.getMessage());
        } finally {
            log.debug("요청 처리 개수: {}", requestCount);
            try {
                connectedSocket.close();
                log.debug("소켓 닫기");
            } catch (IOException e) {
                log.debug("소켓 닫기 실패: " + e.getMessage());
            }
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
        } catch (IllegalStateException e) {
            log.error("Request Line is null");
            return false;
        } catch (SocketException e) { // 클라이언트가 연결 끊었을 때, SocketException("Connection reset") 던짐
            log.error("SocketException " + e.getMessage());
            return false;
        } catch (IOException e) { // 현재 HttpResponse에서 쓰기 도중 예외가 발생된다면, 소켓 연결을 끊어야될 필요가 있다. -> HttpResponse에서 버퍼에 저장했다가 RequestHandler에서 전송
            log.error("IOException " + e.getMessage());
            return false;
        }
    }
}
e