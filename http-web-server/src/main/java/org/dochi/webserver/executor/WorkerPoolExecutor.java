package org.dochi.webserver.executor;

import org.dochi.webserver.config.ThreadPoolConfig;
import org.dochi.webserver.socket.SocketTask;
import org.dochi.webserver.attribute.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class WorkerPoolExecutor {
    private static final Logger log = LoggerFactory.getLogger(WorkerPoolExecutor.class);
    private final ThreadPoolExecutor threadPoolExecutor;

    public WorkerPoolExecutor(ThreadPoolConfig threadPool) {
        this.threadPoolExecutor = new ThreadPoolExecutor(
            threadPool.getMinSpareThreads(),
            threadPool.getMaxThreads(),
            60L, // corePoolSize을 초과하는 스레드가 할당된 작업이 없는 경우 keepAliveTime이 경과한 뒤 제거
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>() // 작업(Task) 대기 큐, 스레드 풀이 모두 바쁠 경우에 추가로 들어오는 작업(SocketTaskHandler)을 일시적으로 저장
        );

        // Core Pool 개수 만큼 스레드를 미리 생성하여 성능 최적화
        threadPoolExecutor.prestartAllCoreThreads();

        log.info("WorkerPoolExecutor initialized [Total size: {}]", threadPoolExecutor.getPoolSize());

        registerShutdownHook();
    }

    // Synchronous
    public SocketTask execute(SocketTask socketTask) {
        Future<?> future = threadPoolExecutor.submit(socketTask);
        try {
            future.get(); // 작업 완료까지 대기
            return socketTask;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while executing socket task: {}", e.getMessage(), e);
            throw new RuntimeException("Execution failed", e);
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownGracefully));
    }

    // 스레드 풀에 남아 있는 대기 중인 작업을 모두 취소
    // 1. 진행 중인 작업이 완료될 때까지 일정 시간 동안 기다림
    // 2. 일정 시간 최과시 스레드 풀이 강제 종료
    private void shutdownGracefully() {
        log.info("Worker thread pool shutdown has started.");
        try {
            // 새로운 작업 수락 중지
            threadPoolExecutor.shutdown();

            // 진행 중인 모든 작업이 완료될 때까지 대기 (true를 반환하면 모든 작업이 종료됨었음을 의미)
            if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                // 최대 60초 후에도 종료되지 않은 경우 강제 종료
                threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) { // 스레드가 대기 중(sleep(), wait(), join() 메서드가 호출된 상태) 인터럽트 신호를 받으면 예외가 발생
            // 인터럽트 발생 시 강제 종료 (더 이상 대기하거나 작업을 계속할 필요가 없다고 판단)
            threadPoolExecutor.shutdownNow();

            // 현재 메서드가 인터럽트를 받았다는 신호를 상위 호출자에게 전달하려면 상태를 복구
            // 현재 스레드의 인터럽트 상태를 true로 설정하고, 상위 호출자는 Thread.currentThread().isInterrupted()로 인터럽트 상태 확인 가능
            Thread.currentThread().interrupt();
            log.error("Worker thread pool shutdown has interrupted.");
        } finally {
            log.info("Worker thread pool shutdown has completed.");
        }
    }
}
