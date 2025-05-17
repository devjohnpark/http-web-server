package org.dochi.webserver.attribute;

public class HttpProcessorAttribute {
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
