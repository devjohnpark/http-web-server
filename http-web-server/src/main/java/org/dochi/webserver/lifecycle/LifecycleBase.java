package org.dochi.webserver.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class LifecycleBase implements Lifecycle {
    private static final Logger log = LoggerFactory.getLogger(LifecycleBase.class);
    private final List<Lifecycle> lifecycles = new ArrayList<>();

    public void addLifeCycle(Lifecycle child) {
        lifecycles.add(child);
    }

    @Override
    public void start() {
        for (Lifecycle lifecycle : lifecycles) {
            lifecycle.init();
            lifecycle.start(); // 하위 컴포넌트 시작
            log.debug("{} initialized and started.", lifecycle.getClass().getSimpleName());
        }
    }

    @Override
    public void stop() {
        for (Lifecycle lifecycle : lifecycles) {
            lifecycle.destroy();
            lifecycle.stop(); // 하위 컴포넌트 중지
            log.debug("{} destroyed and stopped.", lifecycle.getClass().getSimpleName());
        }
    }

    @Override
    public void init() {
        // Noting by default
    }

    @Override
    public void destroy() {
        // Noting by default
    }
}
