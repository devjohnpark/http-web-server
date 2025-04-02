package org.dochi.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HttpParser {
    // return -1: could not get data in buffer
    // 1. EOF
    // 2. buffer.capacity() - buffer.limit(), len is 0
    protected static int readByte(InputBuffer inputBuffer, ByteBuffer buffer) throws IOException {
        // doRead == 0 이면, len == 0 일때이다.
        // len == 0 값이 들어올경우

        // 버퍼에 남은 데이터가 없을때 + 버퍼링
        // 이때 버퍼링한 값이 -1이면, EOF 이므로 그대로 -1 반환
        // 버퍼링한 값이 0 이면, 버퍼에 남은 데이터도 없으므로 무엇을 반환해야하나. -1을 반환하면 더이상 읽을 값이 없단 의미이다.,
        if (!buffer.hasRemaining() && inputBuffer.doRead(buffer) <= 0) {
            return -1;
        }
        return buffer.get() & 0xFF;
    }
}
