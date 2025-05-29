package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.KeepAlive;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioSocketWrapperConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(BioSocketWrapperConnectionTest.class);

    protected volatile BioSocketWrapper serverConnectedSocket;
    protected BioSocketWrapper clientConnectedSocket;

    private Thread serverThread;
    private ServerSocket serverSocket;

    @BeforeEach
    void connect() throws IOException {

        serverSocket = new ServerSocket(0); // 사용 가능한 포트 자동 할당

        serverThread = new Thread(() -> {
            log.debug("Start accept thread...");
            if (!serverSocket.isClosed()) {
                try {
                    serverConnectedSocket = new BioSocketWrapper(serverSocket.accept(), new KeepAlive());
                    log.debug("created server's connection socket");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    log.debug("End accept thread");
                }
            }
        });

        serverThread.start();

        clientConnectedSocket = new BioSocketWrapper(new Socket("localhost", serverSocket.getLocalPort()), new KeepAlive());

        log.debug("Created client's connection socket");

        // JVM이 빈 while 루프를 최적화하여 serverConnectedSocket 값을 캐시에서만 읽을 수 있음
        // 다른 스레드(serverThread)에서 변경한 값이 main 스레드에서 반영되지 않아서 무한 루프를 돌았다.
        // volatile 사용해서 serverConnectedSocket 선언: 값을 쓸 때도 항상 메인 메모리에 즉시 반영 -> 값을 읽을 때 항상 메인 메모리(main memory) 읽음
        while (serverConnectedSocket == null) {
            Thread.onSpinWait(); // 무한 루프 -> CPU 자원을 계속 낭비 -> 과도한 CPU 사용 방지 -> 루프가 busy-wait임을 알려 JIT이 최적화
            // 효과: CPU 파이프라인 효율성 향상, 하이퍼스레딩 환경에서 다른 스레드에게 리소스 양보, 전력 소비 감소
        }

        log.debug("Client and Server connection ready.");
    }

    @AfterEach
    void disconnect() throws IOException {
        if (clientConnectedSocket != null && !clientConnectedSocket.isClosed()) {
            clientConnectedSocket.close();
            log.debug("Client socket close");
        }

        if (serverConnectedSocket != null && !serverConnectedSocket.isClosed()) {
            serverConnectedSocket.close();
            log.debug("Server socket wrapper close");
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            log.debug("Server socket close");
        }

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            log.debug("server thread interrupted");
        }
    }
}