package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.processor.HttpProcessor;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SocketTaskHandler implements SocketTask {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskHandler.class);
    private SocketWrapperBase<?> socketWrapper;
    private final HttpApiMapper ApiMapper; // 동적 HTTP API 추가를 위해 웹서버 인스턴스의 싱글톤 InternalAdapter 주입
    private final HttpProtocolHandler protocolHandler;

    public SocketTaskHandler(HttpProtocolHandler protocolHandler, HttpApiMapper ApiMapper) {
        this.ApiMapper = ApiMapper;
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void run() {
        HttpProcessor processor = this.protocolHandler.getProcessor();
        try {
            getSocketWrapper().startConnectionTimeout(socketWrapper.getKeepAliveTimeout());
            SocketState state = this.protocolHandler.getProcessor().process(socketWrapper, ApiMapper);
            if (state == SocketState.CLOSED) {
                terminate(processor);
            } else if (state ==  SocketState.UPGRADING) {
//                1. 파싱된 요청 데이터 객체의 복사본을 가지고 헤더에서 h2 관련 데이터 가져와서 HTTP/2 설정
//                2. 프로토콜 업그레이드 요청시 프로세서 변경 후 소켓 입출력 스트림 주입하고 process 메서드 호출
//                Http2Processor.process(socketWrapper, httpApiMapper);
//                log.debug("Upgrading socket [Client: {}, Port: {}]", socket.getInetAddress(), socket);
            }
        } catch (IOException e) {
            log.error("Set connection timeout but socket is already closed: {}", e.getMessage());
            terminate(processor);
        } catch (RuntimeException e) {
            log.error("Socket RuntimeException: {}", e.getMessage());
            terminate(processor);
        }
    }

    // SocketTaskHandler 재사용 -> 닫기 수행 -> getSocketWrapper 호출
    private void terminate(HttpProcessor processor) {
        try {
            getSocketWrapper().close();
        } catch (IOException e) {
            log.error("Failed to close socket - Socket State CLOSED: {}", e.getMessage());
        } finally {
            protocolHandler.release(processor);
            socketWrapper = null; // SocketTaskHandler 구현체는 풀링되어 SocketWrapperBase 구현체가 메모리 누수되므로 null 값 할당
        }
    }

    // 재활용되는 객체일때 문제 발생: 재활용 이후 setSocketWrapper 호출을 안하는 것으로 의심됨
    @Override
    public SocketWrapperBase<?> getSocketWrapper() {
        if (socketWrapper == null) {
            throw new IllegalStateException("Socket wrapper is not initialized");
        }
        return socketWrapper;
    }

    @Override
    public void setSocketWrapper(SocketWrapperBase<?> socketWrapper) {
        if (socketWrapper == null) {
            throw new IllegalStateException(getClass().getName() + ": Socket wrapper is null");
        }
        this.socketWrapper = socketWrapper;
    }
}
