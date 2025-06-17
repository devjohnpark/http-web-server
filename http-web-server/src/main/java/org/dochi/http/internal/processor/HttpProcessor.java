package org.dochi.http.internal.processor;

//import org.dochi.http.api.InternalAdapter;
//import org.dochi.http.api.HttpApiMapper;
import org.dochi.http.api.HttpApiMapper;
import org.dochi.webserver.socket.SocketWrapperBase;
import org.dochi.webserver.socket.SocketState;

public interface HttpProcessor {
    SocketState process(SocketWrapperBase<?> socketWrapper, HttpApiMapper httpApiMapper);
}
