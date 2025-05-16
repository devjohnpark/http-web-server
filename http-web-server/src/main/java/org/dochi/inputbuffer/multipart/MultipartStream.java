package org.dochi.inputbuffer.multipart;

import org.dochi.http.request.stream.Http11RequestStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MultipartStream {
    private static final Logger log = LoggerFactory.getLogger(MultipartStream.class);
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Feed
    private final ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
    private final InputStream in;
    private static final int[] EMPTY_CRLF = { CR, LF, CR, LF };

    public MultipartStream(InputStream in) {
        this.in = in;
    }

    // \r\n\r\n까지 읽고 반환 혹은 \r\n\r\n을 제외하고 반환
    // String.split(\r\n)으로 나눠서 header 저장 (버퍼 복사가 없어서 더 빠름)
    //
//    public byte[] readHeaders(int maxHeaderSize) throws IOException {
//        baos.reset();
//        int previousByte = -1;
//        int currentByte;
//        int cnt = 0;
//        int i = 0;
//        int b;
//        // 연속해서 \r\n\r\n
//        for (int size = 0; size < EMPTY_CRLF.length; baos.write(b)) {
//            b = in.read();
//            if (b < 0) {
//                return null;
//            }
//            size++;
//            if (size > maxHeaderSize) {
//                throw new IllegalStateException("Multipart header size exceed the limit bytes: " + maxHeaderSize);
//            }
//
//            if (b == EMPTY_CRLF[i]) {
//                ++i;
//            } else {
//                i = 0;
//            }
//        }
//        return baos.toByteArray(); // or trimBuffer(baos, baos.size() - 4);
//    }

    public byte[] readCRLFLine(int maxSize) throws IOException {
        lineBuffer.reset();
        int previousByte = -1;
        int currentByte;

        while ((currentByte = in.read()) != -1) {

            // CR+LF 조합을 찾으면 줄의 끝
            if (previousByte == CR && currentByte == LF) {
                // 마지막 CRLF 문자 제거
                return lineBuffer.toByteArray();
            }

            if (previousByte != -1) {
                if (lineBuffer.size() >= maxSize) {
                    throw new IllegalStateException("max size exceeds the limit bytes: " + maxSize);
                }
                lineBuffer.write(previousByte);
            }

            previousByte = currentByte;
        }
        return null;
    }
}