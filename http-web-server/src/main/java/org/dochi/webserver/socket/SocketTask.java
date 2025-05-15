package org.dochi.webserver.socket;

import org.dochi.inputbuffer.socket.SocketWrapperBase;

public interface SocketTask extends Runnable {
//    SocketWrapper getSocketWrapper();

    SocketWrapperBase<?> getSocketWrapper();
    void setSocketWrapper(SocketWrapperBase<?> socketWrapper);
}
