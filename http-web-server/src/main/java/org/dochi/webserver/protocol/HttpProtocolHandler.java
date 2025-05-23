package org.dochi.webserver.protocol;

import org.dochi.processor.Http11Processor;
import org.dochi.processor.HttpProcessor;
import org.dochi.webserver.attribute.HttpProcessorAttribute;
import org.dochi.webserver.config.HttpConfig;

import java.util.concurrent.ConcurrentLinkedDeque;

public class HttpProtocolHandler {
    private final HttpConfig config;
    private final ConcurrentLinkedDeque<HttpProcessor> processorPool;

    public HttpProtocolHandler(HttpConfig config, HttpProcessorAttribute attribute) {
        this.config = config;
        // 락 프리(lock-free) 알고리즘을 사용하여 높은 동시성 성능: CAS(Compare-And-Swap) 연산으로 CAS는 CPU가 메모리 위치(V)의 현재 값을 읽어서 메모리 값을 기대 값과 비교해 일치하면 새 값으로 원자적으로 교체하는 동시성 기법
        // Deque 구현체로 LIFO 방식으로 사용할 수 있음 (addFirst로 추가, pollFirst로 제거) -> 최근에 사용된 객체를 우선 재사용하여 캐시 지역성을 활용
        this.processorPool = new ConcurrentLinkedDeque<>();
        for (int i = 0; i < attribute.getPoolSize(); i++) {
            processorPool.offer(new Http11Processor(config));
        }
    }

    public HttpProcessor getProcessor() {
        // pop()은 비어있을 때 예외를 발생시키므로 pollFirst() 사용
        HttpProcessor processor = processorPool.pollFirst();
        if (processor == null) {
            processor = new Http11Processor(config);
        }
        return processor;
    }

    // 사용이 끝난 HttpProcessorAttribute 객체를 풀에 반환
    public void release(HttpProcessor processor) {
        if (processor != null) {
            // 맨 앞에 추가하여 LIFO 방식으로 관리
            processorPool.addFirst(processor);
        }
    }

    // need for test
    public int getSize() {
        return processorPool.size();
    }
}