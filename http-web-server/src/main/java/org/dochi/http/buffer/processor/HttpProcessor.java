package org.dochi.http.buffer.processor;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.buffer.api.HttpApiMapper;
import org.dochi.inputbuffer.socket.SocketWrapperBase;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
//    SocketState process(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper);
    SocketState process(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper);
//    void recycle() throws IOException;
//    SocketState process(SocketWrapperBase<?> socketWrapper, InternalAdapter httpApiMapper);
}
