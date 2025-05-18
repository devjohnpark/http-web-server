package org.dochi.webserver.socket;

import org.dochi.inputbuffer.socket.SocketWrapperBase;

public interface SocketTask extends Runnable {
    SocketWrapperBase<?> getSocketWrapper();
    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
}
