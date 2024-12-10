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

// RequsetHandler
// 사용자 요청을 처리하는 스레드로 응답을 처리한다.
// HTTP Message의 header에 응답 status 상태와 body에 Hello World!를 저장해서 응답한다.
// 이때, Java의 I/O 스트림을 사용해서 소켓을 통해 데이터 송수신한다.
// 클라이언트와 연결된 소켓을 닫기 (커널 영역에 할당된 I/O 자원을 해제)

public class RequestHandlerTimeout1 extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandlerTimeout1.class);
    private final Socket connectedSocket;
    private final RequestMapper requestMapper;

    private static final int KEEP_ALIVE_TIMEOUT = 10000; // 10초 타임아웃
    private static final int MAX_KEEP_ALIVE_REQUESTS = 100000; // 최대 요청 수

    public RequestHandlerTimeout1(Socket connectedSocket, RequestMapper requestMapper) {
        this.connectedSocket = connectedSocket;
        this.requestMapper = requestMapper;
    }

    // HttpProcessor
    @Override
    public void run() {
        log.debug("New client connected IP: {}, Port: {}", connectedSocket.getInetAddress(), connectedSocket.getPort());
        int requestCount = 0;

        try {
            // 소켓 읽기 타임아웃 설정
            connectedSocket.setSoTimeout(KEEP_ALIVE_TIMEOUT);

            // 스트림 생성
            InputStream in = connectedSocket.getInputStream();
            OutputStream out = connectedSocket.getOutputStream();

            while (requestCount < MAX_KEEP_ALIVE_REQUESTS) {
                try {
                    log.debug("LOOP");
                    // HTTP 요청 처리
                    if (!processRequest(in, out)) {
                        break; // 클라이언트가 Connection: close를 명시한 경우
                    }
                    requestCount++;
                }  catch (IOException e) {
                    log.debug("I/O 에러 발생: " + e.getMessage());
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("타임아웃 발생: 연결 종료");
            // Socket의 입출력 시간 설정인, setSoTimeout이 만료된 후에 입출력 수행하면 발생
            // 1. 서버가 요청을 처리하는 동안 타임아웃이 발생: 408 Request Timeout 응답
            // 2. 소켓 닫아서 클라이언트와의 연결은 4-way handshake를 통해 정상적으로 종료된다.
            // 3. 클라이언트는 커넥션이 끊어지면, 요청을 반복해서 보내도 문제가 없는 경우에 요청을 다시 보낸다.
//            return false; // 타임아웃 발생 시 연결 종료
        } catch (IOException e) {
            log.debug("연결 처리 중 에러: " + e.getMessage());
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

    private boolean processRequest(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder request = new StringBuilder();
        String line;

        log.debug("processRequest");

        // blocking method
        line = reader.readLine();
        // 요청이 없을 경우 false 반환
        if (line == null) {
            log.debug("Request Line is null");
            return false;
        }

        String[] reqLine = line.split(" ");

        // read all headers
        while (!(line = reader.readLine()).isEmpty()) {

        }


        // 요청 처리 및 응답 작성
        log.debug("요청:\n {}", request);
        WebResourceProvider webResourceProvider = new WebResourceProvider("webapp");
        Resource resource = webResourceProvider.getResource(reqLine[1]);
        String response = String.format("""
                HTTP/1.1 200 OK
                Content-Type: %s
                Content-Length: %d
                Connection: keep-alive

                """, resource.getContentType(), resource.getData().length);
        out.write(response.getBytes());
        out.write(resource.getData());
        out.flush();

        // Connection: close 헤더가 있는 경우 false 반환
        return !request.toString().contains("Connection: close");
    }

}
