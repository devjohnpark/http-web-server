package org.dochi.external;

import java.io.IOException;
import java.io.InputStream;

public class InternalInputStream extends InputStream {
    private InputBuffer inputBuffer;

    public InternalInputStream(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    // internal 계층에서 동적으로 InputBuffer 구현체가 변경될수 있기 때문에 getInputStream()으로 개발자에게 객체를 넘긴 후에 clear 해줘야한다.
    public void clear() {
        this.inputBuffer = null;
    }

    @Override
    public int read() throws IOException {
        return inputBuffer.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputBuffer.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputBuffer.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputBuffer.close();
    }
}
