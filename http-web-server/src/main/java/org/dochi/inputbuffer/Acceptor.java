package org.dochi.inputbuffer;

import org.dochi.webserver.socket.SocketTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Acceptor로 변경
public class Acceptor {
    private static final Logger log = LoggerFactory.getLogger(Acceptor.class);
    private final ServerSocket listenSocket;

    public Acceptor(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
    }

//    public void connect(SocketTaskExecutor socketTaskExecutor, String hostName, int port) throws IOException {
//        ServerSocket listenSocket = new ServerSocket();
//
//        // 도메인 이름을 IP 주소로 변환해서 유효성 검증한다.
//        // IP가 시스템의 네트워크 인터페이스(eth0)에 할당되어 있어야함 (ip addr로 확인가능)
//        listenSocket.bind(new InetSocketAddress(hostName, port));
//        log.info("Listening client connection request [Host: {}, Port: {}]", hostName, port);
//        Socket establishedSocket;
//        while ((establishedSocket = listenSocket.accept()) != null) {
//            log.info("Accepted new client connection [Client IP: {}, Port: {}]", establishedSocket.getInetAddress(), establishedSocket.getPort());
//            socketTaskExecutor.execute(establishedSocket);
//        }
//    }



    // 기존의 Connector는 ServerSocket 객체에 종속되어 있다.
    // 의존성을 제거하기 위해, 네트워크 끝점(TCP와 APPLICATION) 연결을 처리하는 추상 기본 클래스 AbstractEndpoint 클래스 정의
    // AbstractEndpoint는 여러 추상화 소켓(블로킹/논블로킹 방식)중에서 특정 소켓 사용한다.
    // 자식 클래스의 예: (BioSocketChannel: java.net의 Socket과 ServerSocket을 하나로 묶어 처리할수 있도록 도와주는 클래스)
    // BioEndpoint extends AbstractEndpoint<BioSocketChannel>
    // NioEndpoint extends AbstractEndpoint<NioSocketChannel>

    // Connector을 Acceptor<AbstractEndpoint>(혹은 Acceptor<E serverSocket>)로 변경해서 클라이언트의 연결 요청을 받아들이는 acceptor로 정의
    // Acceptor는 AbstractEndpoint 클래스에 의존하고 BioEndpoint.getServerSocket().accept()을 호출

    // 코드 분석을 더 해봐야함 (아직 구현할려고 정해놓지는 않았ㅇ음)
    // 프로토콜 핸들링 객체(ProtocolHandler)와 클라이언트 요청 처리 객체(HttpProcessor)를 adapte 하는 CoyoteAdapter 객체을 Connector(LifecycleMBeanBase)라고 설정
    // 기본 프로토콜을 HTTP/1.1로 설정(Acceptor(protocol))해서 Http11Processor 인스턴스로 초기화로 준비 -> 이후 프로토콜 업그레이드

    public void accept(SocketTaskExecutor socketTaskExecutor, String hostName, int port) throws IOException {
//        try(ServerSocket listenSocket = new ServerSocket()) {
//            // 도메인 이름을 IP 주소로 변환해서 유효성 검증한다.
//            // IP가 시스템의 네트워크 인터페이스(eth0)에 할당되어 있어야함 (ip addr로 확인가능)
//            listenSocket.bind(new InetSocketAddress(hostName, port));
//            log.info("Listening client connection request [Host: {}, Port: {}]", hostName, port);
//            Socket establishedSocket;
//            while ((establishedSocket = listenSocket.accept()) != null) {
//                log.info("Accepted new client connection [Client IP: {}, Port: {}]", establishedSocket.getInetAddress(), establishedSocket.getPort());
//                socketTaskExecutor.execute(establishedSocket);
//            }
//        }
        // 도메인 이름을 IP 주소로 변환해서 유효성 검증한다.
        // IP가 시스템의 네트워크 인터페이스(eth0)에 할당되어 있어야함 (ip addr로 확인가능)
        listenSocket.bind(new InetSocketAddress(hostName, port));
        log.info("Listening client connection request [Host: {}, Port: {}]", hostName, port);
        Socket establishedSocket;
        while ((establishedSocket = listenSocket.accept()) != null) {
            log.info("Accepted new client connection [Client IP: {}, Port: {}]", establishedSocket.getInetAddress(), establishedSocket.getPort());
            socketTaskExecutor.execute(establishedSocket);
        }
    }
}
