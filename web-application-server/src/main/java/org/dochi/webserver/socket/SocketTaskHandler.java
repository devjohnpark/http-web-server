package org.dochi.webserver.socket;

import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.internal.processor.HttpProcessor;
import org.dochi.webserver.protocol.HttpProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class SocketTaskHandler implements SocketTask {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskHandler.class);
    private SocketWrapperBase<?> socketWrapper;
    private final HttpApiMapper apiMapper;
    private final HttpProtocolHandler protocolHandler;

    public SocketTaskHandler(HttpProtocolHandler protocolHandler, HttpApiMapper ApiMapper) {
        this.apiMapper = ApiMapper;
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void run() {
        try {
            SocketState state = SocketState.OPEN;
            HttpProcessor processor = this.protocolHandler.getProcessor(); // 기본 default HTTP/1.1;
            getSocketWrapper().setConnectionTimeout(socketWrapper.getConfigConnectionTimeout());
            while (state == SocketState.OPEN) {
                state = processor.process(socketWrapper, apiMapper);
                if (state == SocketState.CLOSED) {
                    protocolHandler.release(processor);
                } else if (state == SocketState.UPGRADING) {
                    // 1. 파싱된 요청 데이터 객체(internal.Request)의 복사본을 가지고 헤더에서 h2 관련 데이터 가져와서(AbstractProcessor.getUpgradeToken()) HTTP/2 설정
                    // 2. 필요한 스트림의 개수 만큼 Http2Processor 생성
                    // 3. 소켓 연결 시간 다시 설정

                    // 다음 코드만 추가하고, 필요한 스트림 개수만큼 while 문 반복
                    // ulti stream 처리를 위해 process 비동기 메서드로 변환필요, process 실행 이후 동기적으로 release 메서드 호출 필요
                    // processor = this.protocolHandler.getProcessor("HTTP/2.0");
                }

            }
        } catch (IOException e) {
            log.error("Set connection timeout but socket is already closed: ", e);
        } finally {
            terminate();
        }
    }

    private void terminate() {
        try {
            getSocketWrapper().close();
        } catch (IOException e) {
            log.error("Failed to close socket - Socket State CLOSED: {}", e.getMessage());
        } finally {
            socketWrapper = null; // SocketTaskHandler 구현체는 풀링되어 큐에 저장되므로 SocketWrapperBase 구현체가 메모리 낭비되므로 null 값 할당
        }
    }

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
