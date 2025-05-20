package org.dochi.webserver.socket;

public interface SocketTask extends Runnable {
    SocketWrapperBase<?> getSocketWrapper();
    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
}
