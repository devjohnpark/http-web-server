package org.dochi.webserver.attribute;

import org.dochi.webserver.config.HttpProcessorConfig;

public class HttpProcessorAttribute implements HttpProcessorConfig {
    private static final int DEFAULT_PROCESSOR_POOL_SIZE = 10;
    private int poolSize = DEFAULT_PROCESSOR_POOL_SIZE;

    public void setPoolSize(int poolSize) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("thread pool size cannot be less than 1");
        }
        this.poolSize = poolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
