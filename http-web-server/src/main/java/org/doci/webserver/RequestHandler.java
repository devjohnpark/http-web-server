package org.doci.webserver;

import org.doci.http.api.HttpApiHandler;
import org.doci.http.request.HttpRequest;
import org.doci.http.request.HttpVersion;
import org.doci.http.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

// RequsetHandler
// 사용자 요청을 처리하는 스레드로 응답을 처리한다.
// HTTP Message의 header에 응답 status 상태와 body에 Hello World!를 저장해서 응답한다.
// 이때, Java의 I/O 스트림을 사용해서 소켓을 통해 데이터 송수신한다.
// 클라이언트와 연결된 소켓을 닫기 (커널 영역에 할당된 I/O 자원을 해제)

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final Socket connectedSocket;
    private final RequestMapper requestMapper;

    public RequestHandler(Socket connectedSocket, RequestMapper requestMapper) {
        this.connectedSocket = connectedSocket;
        this.requestMapper = requestMapper;
    }

    // 1. 클라이언트 요청 헤더에서 HTTP Method과 리소스 경로 확인
    // 2. 클라이언트 요청 헤더에서 리소스 읽기 (리소스의 디렉토리 없으면 400)
    // 3. 해당 경로의 리소스의 데이터 가져오기 (리소스 가져오기 실패시 404/리소스 가져오기 성공시 200)
    // 4. 응답 헤더 쓰기
    // 5. 응답 바디 쓰기

    // RequestHandler 클래스에 역할이 집중되어 테스트하기가 어려짐 -> 객체 역할 분리해서 private 메서드가 아닌 public 메서드로 되어 테스트 유리
    // 클라이언틍 요청에 대한 처리 (RequestHandler)
    // 클라이언트 요청 데이터 처리 (HttpRequest)
    // 클라이언트 응답 데이터 처리(HttpResponse)
    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        try (
                // Socket(TCP) Buffer에 저장된 데이터를 읽기 위한 InputStream을 제공, 이 스트림을 통해 클라이언트로 부터 받은 데이터 받아올수 있다.
                InputStream in = connectedSocket.getInputStream();
                // Socket(TCP) Buffer에 저장된 데이터를 쓰기 위한 OutputStream을 제공, 이 스트림을 통해 클라이언트에게 데이터를 보낼수 있다.
                OutputStream out = connectedSocket.getOutputStream();
        ) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            HttpApiHandler httpApiHandler = requestMapper.getHttpApiHandler(request.getPath());
            httpApiHandler.handleApi(request, response);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

