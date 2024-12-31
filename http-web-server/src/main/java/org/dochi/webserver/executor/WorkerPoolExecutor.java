package org.dochi.webserver.executor;

import org.dochi.webserver.RequestHandler;
import org.dochi.webserver.RequestHandlerPool;
import org.dochi.webserver.config.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerPoolExecutor {
    private static final Logger log = LoggerFactory.getLogger(WorkerPoolExecutor.class);
    private final ExecutorService threadPool;

    public WorkerPoolExecutor(ThreadPool threadPool) {
        this.threadPool = new ThreadPoolExecutor(
            threadPool.getMinSpareThreads(),
            threadPool.getMaxThreads(),
            60000L, // corePoolSize를 초과하는 추가 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>() // 작업(Task) 대기 큐, 스레드 풀이 모두 바쁠 경우에 추가로 들어오는 작업(Runnable)을 일시적으로 저장
        );
    }

    public void executeRequest(RequestHandler requestHandler, RequestHandlerPool requestHandlerPool) {
        // 직접 run() 호출: run()을 직접 호출하면 스레드 풀이나 새 스레드와 무관하게 현재 실행 중인 스레드에서 실행된다.
        // threadPool.execute()가 스레드 풀의 워커 스레드에 작업을 넘겨서 실행하므로 requestHandler.run()이 새로운 스레드에서 동작하도록 만든다.
        // 만약 threadPool.execute 없이 run()을 호출하면, 멀티스레드로 적용되지 않고 현재 실행중인 스레드의 스택에서 그대로 실행된다.
        threadPool.execute(() -> {
            try {
                requestHandler.run();
            } finally {
                // 작업 완료 후 RequestHandler를 큐에 반환
                requestHandlerPool.recycleRequestHandler(requestHandler);
            }
        });
    }
}
