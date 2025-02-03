package org.dochi.webserver.socket;

import org.dochi.webserver.executor.WorkerPoolExecutor;

import java.net.Socket;

public class SocketTaskExecutor {

    private final WorkerPoolExecutor workerExecutor;
    private final SocketTaskPool taskPool;

    public SocketTaskExecutor(WorkerPoolExecutor workerExecutor, SocketTaskPool taskPool) {
        this.workerExecutor = workerExecutor;
        this.taskPool = taskPool;
    }

    public void execute(Socket connectedSocket) {
        // 사용가능한 SocketTask 구현체 가져오기
        SocketTask socketTask = taskPool.get();

        // 새롭게 연결된 소켓 설정
        socketTask.getSocketWrapper().setConnectedSocket(connectedSocket);

        // WorkerPoolExecutor 객체로 SocketTask 구현체를 ThreadPoolExecutor 객체에 제출해서 비동기 실행
        // SocketTask 객체 ThreadPoolExecutor 객체에 제출 후 즉각 SocketTask 구현체 반환
        // 반환된 SocketTask 구현체 RequestTaskPool의 큐에 푸시
        taskPool.recycle(workerExecutor.execute(socketTask));
    }
}
