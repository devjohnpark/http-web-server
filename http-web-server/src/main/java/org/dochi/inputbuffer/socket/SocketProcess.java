package org.dochi.inputbuffer.socket;

public interface SocketProcess<E> {
    void doRun();
    SocketWrapperBase<E> getSocketWrapper();
    void setSocketWrapper(SocketWrapperBase<E> socketWrapper);
}
