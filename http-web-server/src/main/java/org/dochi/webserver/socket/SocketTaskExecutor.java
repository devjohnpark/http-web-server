package org.dochi.webserver.socket;

import org.dochi.inputbuffer.socket.SocketWrapperBase;
import org.dochi.webserver.executor.WorkerPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class SocketTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskExecutor.class);

    private final WorkerPoolExecutor workerExecutor;
    private final SocketTaskPool taskPool;

    public SocketTaskExecutor(WorkerPoolExecutor workerExecutor, SocketTaskPool taskPool) {
        this.workerExecutor = workerExecutor;
        this.taskPool = taskPool;
    }

//    public void execute(Socket connectedSocket) {
//        // 사용가능한 SocketTask 구현체 가져오기
//        SocketTask socketTask = taskPool.get();
//
//        // 새롭게 연결된 소켓 설정
//        socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);
//
//        // WorkerPoolExecutor 객체로 SocketTask 구현체를 ThreadPoolExecutor 객체에 제출해서 비동기 실행
//        // SocketTask 객체 ThreadPoolExecutor 객체에 제출 후 즉각 SocketTask 구현체 반환
//        // 반환된 SocketTask 구현체 RequestTaskPool의 큐에 푸시
//        taskPool.recycle(workerExecutor.execute(socketTask));
//    }

//    public void execute(SocketWrapperBase<?> socketWrapper) {
//        // 사용가능한 SocketTask 구현체 가져오기
//        SocketTask socketTask = taskPool.get();
//
//        // 새롭게 연결된 소켓 설정
//        socketTask.setSocketWrapper(socketWrapper);
//
//        // WorkerPoolExecutor 객체로 SocketTask 구현체를 ThreadPoolExecutor 객체에 제출해서 비동기 실행
//        // SocketTask 객체 ThreadPoolExecutor 객체에 제출 후 즉각 SocketTask 구현체 반환
//        // 반환된 SocketTask 구현체 RequestTaskPool의 큐에 푸시
//
//        // 내부저으로 execute 메서드가 비동기로 실행되어 아직 실행 안된 SocketTask 구현체가 객체 풀에 반환된다.
//        // 따라서 맨위 코드에서 객체풀의 SocketTask 구현체를 다시 가져올때 실행하지 않은 SocketTask를 불러올수 있다.
//        taskPool.recycle(workerExecutor.execute(socketTask));
//    }

    public void execute(SocketWrapperBase<?> socketWrapper) {
        CompletableFuture.runAsync(() -> {
            try {
                // 사용 가능한 SocketTask 가져오기
                SocketTask socketTask = taskPool.get();

                // 새로 연결된 소켓 설정
                socketTask.setSocketWrapper(socketWrapper);

//                // WorkerPoolExecutor를 통해 작업 실행
//                workerExecutor.execute(socketTask);

                // 작업 완료 후 객체 풀에 반환
//                log.debug("SocketTaskExecutor before calling workerExecutor.execute");
                taskPool.recycle(workerExecutor.execute(socketTask));
//                log.debug("SocketTaskExecutor after calling workerExecutor.execute");
            } catch (Exception e) {
                log.error("Error executing socket task: {}", e.getMessage(), e);
                throw new RuntimeException("Execution failed", e);
            }
        });
    }
}
