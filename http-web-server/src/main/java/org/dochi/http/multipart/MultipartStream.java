package org.dochi.http.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MultipartStream {
    private static final Logger log = LoggerFactory.getLogger(MultipartStream.class);
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Feed
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final InputStream in;
    private static final int[] EMPTY_CRLF = {CR, LF, CR, LF };

    public MultipartStream(InputStream in) {
        this.in = in;
    }

//    public MultipartStream(InputBuffer inputBuffer) {
//        super(inputBuffer);
//    }

//    public MultipartStream(InputStream in) {
//        super(in);
//    }

//    public String readHeader(MessageSizeMonitor sizeMonitor) throws IOException {
//        try {
//            byte[] lineBytes = readLine(sizeMonitor.getSizeLimit());
//            if (lineBytes != null) {
//                sizeMonitor.monitorSize(getReadLineSize(lineBytes));
//                return new String(lineBytes, StandardCharsets.UTF_8);
//            }
//        } catch (LineTooLongIOException e) {
//            sizeMonitor.monitorSize(e.getLimitLineSize());
//        } catch (NotFoundCrlfIOException e) {
//            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//        return null;
//    }

    // \r\n\r\n까지 읽고 반환 혹은 \r\n\r\n을 제외하고 반환
    // String.split(\r\n)으로 나눠서 header 저장
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
        baos.reset();
        int previousByte = -1;
        int currentByte;

        while ((currentByte = in.read()) != -1) {

            baos.write(currentByte);

            // CR+LF 조합을 찾으면 줄의 끝
            if (previousByte == CR && currentByte == LF) {
                // 마지막 CRLF 문자 제거
                return trimBuffer(baos, baos.size() - 2);
            }

            // line과 부합할때까지 읽은 데이터 크기가 커지는것을 방지
            // lineBuffer: 12345\r1 (7)
            // limitLineSize: 5
            if (baos.size() >= maxSize + 2) {
                throw new IllegalStateException("max size exceeds the limit bytes: " + maxSize);
            }

            previousByte = currentByte;
        }
        return null; // null 반환시 요청 처리 즉각 종료
    }

    // 복사 비용 발생
    private byte[] trimBuffer(ByteArrayOutputStream buffer, int newSize) {
        byte[] trimmedBuffer = new byte[newSize];
        System.arraycopy(buffer.toByteArray(), 0, trimmedBuffer, 0, newSize);
        return trimmedBuffer;
    }

}