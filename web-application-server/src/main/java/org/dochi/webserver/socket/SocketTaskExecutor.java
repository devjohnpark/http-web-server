package org.dochi.webserver.socket;

import org.dochi.webserver.executor.WorkerPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class SocketTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskExecutor.class);

    private final WorkerPoolExecutor workerExecutor;
    private final SocketTaskPool taskPool;

    public SocketTaskExecutor(WorkerPoolExecutor workerExecutor, SocketTaskPool taskPool) {
        this.workerExecutor = workerExecutor;
        this.taskPool = taskPool;
    }

    public void execute(SocketWrapperBase<?> socketWrapper) {
        CompletableFuture.runAsync(() -> {
            try {
                // 사용 가능한 SocketTask 가져오기
                SocketTask socketTask = taskPool.get();

                // 새로 연결된 소켓 설정
                socketTask.setSocketWrapper(socketWrapper);

                // 작업 완료 후 객체 풀에 반환
                taskPool.recycle(workerExecutor.execute(socketTask));
            } catch (Exception e) {
                log.error("Error executing socket task: {}", e.getMessage(), e);
                throw new RuntimeException("Execution failed", e);
            }
        });
    }
}
