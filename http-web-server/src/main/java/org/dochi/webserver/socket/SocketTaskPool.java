package org.dochi.webserver.socket;

import org.dochi.webserver.attribute.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class SocketTaskPool {
    private static final Logger log = LoggerFactory.getLogger(SocketTaskPool.class);
    private final LinkedBlockingQueue<SocketTask> queue;
//    private final HttpApiMapper requestMapper;
//    private final KeepAlive keepAlive;
    private final Supplier<SocketTask> supplier;

    public SocketTaskPool(ThreadPool threadPool, Supplier<SocketTask> supplier) {
        this.queue = new LinkedBlockingQueue<>();
//        this.requestMapper = new HttpApiMapper(httpApiService);
        this.supplier = supplier;
//        this.keepAlive = keepAlive;
        this.initPool(threadPool.getMinSpareThreads(), supplier);
        log.info("SocketTaskPool initialized [Total size: {}]", queue.size());
    }

//    public SocketTaskPool(ThreadPool threadPool, KeepAlive keepAlive, WebService httpApiService) {
//        this.requestHandlerQueue = new LinkedBlockingQueue<>();
//        this.keepAlive = keepAlive;
//        this.requestMapper = new HttpApiMapper(httpApiService);
//        this.initPool(threadPool, keepAlive, requestMapper);
//    }
//
//    public SocketTaskPool(ThreadPool threadPool, KeepAlive keepAlive, HttpApiMapper requestMapper) {
//        this.requestHandlerQueue = new LinkedBlockingQueue<>();
//        this.initPool(threadPool, keepAlive, requestMapper);
//    }

//    private void initPool(ThreadPool threadPool, KeepAlive keepAlive, HttpApiMapper requestMapper) {
//        int poolSize = threadPool.getMinSpareThreads();
//        for (int i = 0; i < poolSize; i++) {
//            requestHandlerQueue.offer(new SocketTaskHandler(new SocketWrapper(keepAlive), requestMapper)); // 큐의 끝에 삽입
//        }
//        log.info("SocketTaskPool initialized, Total size: {}.", requestHandlerQueue.size());
//    }

    private void initPool(int poolSize, Supplier<SocketTask> requestHandlerSupplier) {
        for (int i = 0; i < poolSize; i++) {
            queue.offer(requestHandlerSupplier.get()); // 공급자를 통해 객체 생성 및 큐에 추가
        }
    }

    public SocketTask get() {
        SocketTask socketTask = queue.poll();
        if (socketTask == null || socketTask.getSocketWrapper().isUsing()) {
            socketTask = supplier.get(); // SocketTask 구현체 생성
        }
        return socketTask;
    }

//    public SocketTaskHandler getAvailableRequestHandler(KeepAlive keepAlive, HttpApiMapper requestMapper) {
//        SocketTaskHandler requestHandler = requestHandlerQueue.poll();
//        if (requestHandler == null || requestHandler.getSocketWrapper().isUsing()) {
//            requestHandler = new SocketTaskHandler(new SocketWrapper(keepAlive), requestMapper);
//        }
//        return requestHandler;
//    }

    public void recycle(SocketTask socketTask) {
        queue.offer(socketTask);
    }

    public int getPoolSize() {
        return queue.size();
    }
}
