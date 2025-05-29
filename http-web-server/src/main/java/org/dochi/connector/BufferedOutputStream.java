package org.dochi.connector;

import org.dochi.webserver.socket.SocketWrapperBase;

import java.io.IOException;
import java.io.OutputStream;

public class BufferedOutputStream extends OutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private SocketWrapperBase<?> socketWrapper;
    private final byte[] buffer;
    private int bufferPosition = 0;
    private boolean isBufferEnabled = true;

    public BufferedOutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public void init(SocketWrapperBase<?> socketWrapper) {
        this.socketWrapper = socketWrapper;
    }

    public BufferedOutputStream(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("buffer size need to be positive");
        }
        this.buffer = new byte[bufferSize];
    }

    private final byte[] tmp = new byte[1];

    @Override
    public void write(int b) throws IOException {
        if (isBufferEnabled) {
            writeToBuffer(b);
        } else {
            tmp[0] = (byte) b;
            socketWrapper.write(tmp, 0 , 1);
        }
    }

    private void writeToBuffer(int b) throws IOException {
        if (bufferPosition >= buffer.length) {
            flushBuffer();
        }
        buffer[bufferPosition++] = (byte) b;
    }

    @Override
    public void flush() throws IOException {
        flushBuffer();
    }

    private void flushBuffer() throws IOException {
        if (bufferPosition > 0) {
            socketWrapper.write(buffer, 0, bufferPosition);
            socketWrapper.flush(); // 스트림 버퍼의 데이터를 OS의 네트워크 스택인 TCP(socket) 버퍼에 즉시 전달 보장
            bufferPosition = 0;
        }
    }

    public void recycle() {
        bufferPosition = 0;
        isBufferEnabled = true;
    }

    // 일반 출력 스트림 사용
    public BufferedOutputStream getOutputStream() {
        isBufferEnabled = false;
        return this;
    }
}