package org.dochi.webserver.config;

public class ThreadPool {
    private int minSpareThreads = 10;
    private int maxThreads = 200;

    public void setMinSpareThreads(int minSpareThreads) {
        if (minSpareThreads < 1) {
            throw new IllegalArgumentException("thread pool size cannot be less than 1");
        }
        this.minSpareThreads = minSpareThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("thread pool size cannot be less than 1");
        }
        this.maxThreads = maxThreads;
    }

    public int getMaxThreads() {
        return maxThreads;
    }
}
