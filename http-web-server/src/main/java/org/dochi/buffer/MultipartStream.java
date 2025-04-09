package org.dochi.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;

public class MultipartStream {
    private final InputStream in;
    private byte[] buffer;

    // Body: Buffer를 사용한 FileInputStream을 통해 읽는 즉시 write, write한 최대 크기를 벗어나면 예외처리
    // 헤더를 하나씩 읽어서 :_나 : 기준으로 키 값 분리 후, String으로 저장 (헤더 파라메터는 connecotr.request에서 파싱)
    // 바디를 하나씩 읽는 버퍼에 모았다가 파일로 write (write한 최대 크기를 벗어나면 예외처리)

    public MultipartStream(InputStream in) {
        this.in = in;
    }

//    public int readCrlfLine(byte[] b, int off, int len) throws IOException {
//        if (len <= 0) {
//            return 0;
//        }
//        int count = 0;
//        int cur;
//        int prev = -1;
//        while((cur = this.in.read()) != -1) {
//            b[off++] = (byte)cur;
//            ++count;
//            if (prev == '\r' && cur == '\n') {
//                // remove crlf
//                b[off-1] = 0;
//                b[off-2] = 0;
//                return count; // \r\n 포함/미포함 결정
//            } else if (count == len) {
//                throw new BufferOverflowException();
//            }
//            prev = cur;
//        }
//        return -1;
//    }
}
