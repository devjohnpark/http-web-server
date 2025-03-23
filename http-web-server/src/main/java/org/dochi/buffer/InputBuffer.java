package org.dochi.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface InputBuffer {
    // 내부 버퍼에 바이트 데이터 저장
    // HTTP/1.1: 데이터 읽어서 그대로 내부버퍼에 저장
    // HTTP/2.0: 프레임을 순서대로 조립후 HPACK 압축 해제하여 순서대로 데이터를 나열
    int doRead(ByteBuffer buffer) throws IOException;
    int duplicate(ByteBuffer buffer);
//    ByteBuffer getByteBuffer();

//    int duplicate(ByteBuffer buffer) throws IOException;
//    int getByte() throws IOException;
////    int setBytes(int startIndex, int len);
//    int getByteArray
//    int pos();
}
