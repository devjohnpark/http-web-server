package org.dochi.http.request.stream;

import java.io.IOException;
import java.io.InputStream;

public abstract class HttpBufferedInputStream extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final InputStream inputStream;
    private final byte[] buffer;
    private int bufferPosition = 0;
    private int bufferSize = 0;
    private boolean isBufferEnabled = true;

    protected HttpBufferedInputStream(InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE);
    }

    protected HttpBufferedInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public int read() throws IOException {
        if (shouldBufferEnabled()) {
            return readBuffer();
        }
        return inputStream.read();
    }

    private boolean shouldBufferEnabled() {
        return isBufferEnabled || bufferPosition < bufferSize;
    }

    private int readBuffer() throws IOException {
        if (bufferPosition >= bufferSize) {
            bufferSize = inputStream.read(buffer);
            bufferPosition = 0;
            if (bufferSize == -1) {
                return -1; // EOF
            }
        }
        return buffer[bufferPosition++] & 0xFF; // 1 byte 읽어서 int 형으로 표현
    }

//    @Override
//    public int read() throws IOException {
//        if (bufferPosition >= bufferSize) {
//            bufferSize = inputStream.read(buffer);
//            bufferPosition = 0;
//            if (bufferSize == -1) {
//                return -1; // EOF
//            }
//        }
//        return buffer[bufferPosition++] & 0xFF; // 1 byte 읽어서 int 형으로 표현
//    }

//    public int copyBuffer(byte[] buf) {
//        System.arraycopy(buffer, bufferPosition, buf, 0, bufferSize - bufferPosition);
//        return bufferSize - bufferPosition;
//    }
//
//    public int copyBuffer(char[] cb) {
//        char[] decodedChars = new String(buffer, 0, bufferSize - bufferPosition, StandardCharsets.UTF_8).toCharArray();
//        System.arraycopy(decodedChars, 0, cb, 0, decodedChars.length);
//        return decodedChars.length;
//    }

    public HttpBufferedInputStream getInputStream() {
        isBufferEnabled = false;
        return this;
    }
}
