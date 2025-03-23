package org.dochi.buffer;


public abstract class SocketProcessor<E> implements Runnable {
    private SocketWrapperBase<E> socketWrapper;

    @Override
    public void run() {
        if (socketWrapper != null && !socketWrapper.isClosed()) {
            this.doRun();
        }
    }

    protected abstract void doRun();

    protected void reset(SocketWrapperBase<E> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }
}
