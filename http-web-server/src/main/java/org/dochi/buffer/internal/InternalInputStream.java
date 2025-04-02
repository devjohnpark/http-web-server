package org.dochi.buffer.internal;

import org.dochi.buffer.InputBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;



// java 프로그래머한테 InputStream이 익숙하므로 InputBuffer를 InputStream/Reader로 감싸서 제공
// 헤더를 버퍼링할때 ByteBuffer에서 버퍼링된 바디의 데이터를 복사
// Reader의 경우 byte[]를 char[]로 변환하는 과정이 필요함
// Request는 Reader/InputStream 객체로 개발자에게 제공
// Response는 Writer/OutputStream 객체로 개발자에게 제공

public class InternalInputStream extends InputStream {
    private final static int DEFAULT_BUFFER_SIZE = 8192;
    private final ByteBuffer inputStreamBuffer;

    private final InputBuffer inputBuffer;
    private ByteBuffer sourceBuffer;;
    private boolean isInputStreamBuffer = false;

    public InternalInputStream(InputBuffer inputBuffer) {
        this(inputBuffer, DEFAULT_BUFFER_SIZE);
    }

    public InternalInputStream(InputBuffer inputBuffer, int bufferSize) {
        this.inputStreamBuffer = ByteBuffer.allocate(bufferSize);
        this.inputBuffer = inputBuffer;
        this.sourceBuffer = inputBuffer.getByteBuffer();
    }

    public void recycle() {
        this.isInputStreamBuffer = false;
        this.sourceBuffer = inputBuffer.getByteBuffer();
        inputStreamBuffer.position(0);
        inputStreamBuffer.limit(0);
    }

    private boolean shouldInputStreamBuffer() {
        // 연산해야하므로 boolean 값으로 확인하자
        if (!isInputStreamBuffer && !inputBuffer.getByteBuffer().hasRemaining()) {
            inputStreamBuffer.flip();
            sourceBuffer = inputStreamBuffer;
            isInputStreamBuffer = true;
        }
        return isInputStreamBuffer;
    }


    private int fill() throws IOException {
        if (inputStreamBuffer.hasRemaining()) {
            return 0;
        }
        if (inputStreamBuffer.capacity() - inputStreamBuffer.limit() >= 0) {
            inputStreamBuffer.position(0);
            inputStreamBuffer.limit(0);
        }
        int n = 0;
        if ((n = inputBuffer.doRead(inputStreamBuffer)) <= 0) {
            return -1;
        }
        return n;
    }

    // read(): 시스템 콜 비용을 줄이기 위해서 내부 버퍼 사용
    // read(byte[] b, int off, int len): 매개변수로 받은 버퍼에 읽기를 사용 (단, 내부 버퍼를 데이터가 남아있으면 복사 후 읽기)
    // 내부 버퍼의 종류는 InputBuffer/InputStream 버퍼가 있다.

    // multipart/form-data 일때 하나씩 데이터를 읽어가주고 파싱해서 헤더 요소를 String으로 저장하고
    // 바디는 텍스트가 아닌 파일의 경우 용량이 크기 때문에 파일로 저장하고 텍스트는 애플리케이션 메모리에 저장
    @Override
    public int read() throws IOException {
        if (shouldInputStreamBuffer() && fill() < 0) {
            return -1;
        }
        return sourceBuffer.get() & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        shouldInputStreamBuffer(); // InputStream/InputBuffer 중에서 버퍼 변경 필요하면 변경
        int fillCnt = 0;
        if (sourceBuffer.hasRemaining()) { // 남은 버퍼의 데이터 복사
            fillCnt = Math.min(sourceBuffer.remaining(), len);
            sourceBuffer.get(b, off, fillCnt);
        } else { // 소켓 버퍼에서 매개변수 버퍼로 버퍼링
            ByteBuffer byteBuffer = ByteBuffer.wrap(b, off, len);
            byteBuffer.flip();
            fillCnt = inputBuffer.doRead(byteBuffer);
            if (fillCnt < 0) {
                return -1;
            }
        }
        return fillCnt;
    }

//    // 매개변수로 받은 버퍼의 길이 만큼 못채웠을때 소켓 버퍼링을 통해 추가 읽기
//    @Override
//    public int read(byte[] b, int off, int len) throws IOException {
//        if (b == null) {
//            throw new NullPointerException();
//        }
//        if (off < 0 || len < 0 || off + len > b.length) {
//            throw new IndexOutOfBoundsException();
//        }
//        if (len == 0) {
//            return 0;
//        }
//        shouldInputStreamBuffer(); // InputStream/InputBuffer 중에서 버퍼 변경 필요하면 변경
//        int fillCnt = 0;
//        if (sourceBuffer.hasRemaining()) { // 남은 버퍼의 데이터 복사
//            fillCnt = Math.min(sourceBuffer.remaining(), len);
//            sourceBuffer.get(b, off, fillCnt);
//        }
//        // 소켓 버퍼에서 매개변수 버퍼로 버퍼링
//        if (fillCnt < len) {
//            ByteBuffer byteBuffer = ByteBuffer.wrap(b, off + fillCnt, len - fillCnt);
//            byteBuffer.flip();
//            int n = inputBuffer.doRead(byteBuffer);
//            if (n < 0) {
//                return -1;
//            }
//            fillCnt += n;
//        }
//        return fillCnt;
//    }

}
