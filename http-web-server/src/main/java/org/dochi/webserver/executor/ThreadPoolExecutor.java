package org.dochi.webserver.executor;

import org.dochi.webserver.RequestHandler;
import org.dochi.webserver.RequestMapper;
import org.dochi.webserver.SocketWrapper;
import org.dochi.webserver.config.ServerConfig;
import org.dochi.webserver.config.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutor {
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecutor.class);
    private final ExecutorService threadPool;
    private final LinkedBlockingQueue<RequestHandler> requestHandlerQueue;

    public ThreadPoolExecutor(ThreadPool threadPool) {
        this.threadPool = new java.util.concurrent.ThreadPoolExecutor(
            threadPool.getMinSpareThreads(),
            threadPool.getMaxThreads(),
            60000L, // corePoolSize를 초과하는 추가 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>() // 작업(Task) 대기 큐, 스레드 풀이 모두 바쁠 경우에 추가로 들어오는 작업(Runnable)을 일시적으로 저장
        );
        this.requestHandlerQueue = new LinkedBlockingQueue<>();
    }

    public void executeRequestHandler(Socket establishedSocket, RequestMapper requestMapper, ServerConfig serverConfig) {
        // 큐에서 사용 가능한 RequestHandler 가져오기
        RequestHandler requestHandler = getAvailableRequestHandler(requestMapper, serverConfig);

        // 소켓 설정
        configureSocket(requestHandler, establishedSocket);

        // 워커 스레드 실행
        executeRequestHandler(requestHandler);
    }

    public void initThreadPool(ServerConfig serverConfig, RequestMapper requestMapper) {
        int poolSize = serverConfig.getThreadPool().getMinSpareThreads();
        for (int i = 0; i < poolSize; i++) {
            requestHandlerQueue.offer(new RequestHandler(new SocketWrapper(serverConfig.getKeepAlive()), requestMapper)); // 큐의 끝에 삽입
        }
    }

    private RequestHandler getAvailableRequestHandler(RequestMapper requestMapper, ServerConfig serverConfig) {
        RequestHandler requestHandler = requestHandlerQueue.poll();
        if (requestHandler == null || requestHandler.getSocketWrapper().isConnected()) {
            // 새로운 RequestHandler 생성
            requestHandler = new RequestHandler(new SocketWrapper(serverConfig.getKeepAlive()), requestMapper);
        }
        return requestHandler;
    }

    private void configureSocket(RequestHandler requestHandler, Socket establishedSocket) {
        requestHandler.getSocketWrapper().setSocket(establishedSocket);
    }

    private void executeRequestHandler(RequestHandler requestHandler) {
        // 직접 run() 호출: run()을 직접 호출하면 스레드 풀이나 새 스레드와 무관하게 현재 실행 중인 스레드에서 실행된다.
        // threadPool.execute()가 스레드 풀의 워커 스레드에 작업을 넘겨서 실행하므로 requestHandler.run()이 새로운 스레드에서 동작하도록 만든다.
        // 만약 threadPool.execute 없이 run()을 호출하면, 멀티스레드로 적용되지 않고 현재 실행중인 스레드의 스택에서 그대로 실행된다.
        threadPool.execute(() -> {
            try {
                requestHandler.run();
            } finally {
                // 작업 완료 후 RequestHandler를 큐에 반환
                requestHandlerQueue.offer(requestHandler);
            }
        });
    }
}
