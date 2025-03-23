//package org.dochi.buffer;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.BufferOverflowException;
//import java.nio.ByteBuffer;
//
//
//// InputStream 주입 대신 BioSocketWrapper 주입
//// InputStream
//public class SocketInputBuffer implements InputBuffer {
//    private static final int DEFAULT_READ_BUFFER_SIZE = 8192;
//
//    private SocketWrapperBase<?> socketWrapper;
//
//    // BioSocketWrapper 객체로 대체
//    private final InputStream inputStream;
//
//
////    // InputStream을 주입받지 알고 SocketWrapper를 주입받아서 호환성 챙긴다.
//    public SocketInputBuffer(InputStream inputStream) {
//        this.inputStream = inputStream;
//    }
//
//    public boolean hasSocket() {
//        return socketWrapper != null;
//    }
//
//    public void init(SocketWrapperBase<?> socketWrapper) {
//        this.socketWrapper = socketWrapper;
//    }
//
//    public void recycle() {
//        this.socketWrapper = null;
//    }
//
//
//    // 1. 버퍼의 최대 크기만큼 한번에 읽어서 초기화(?) 하지만 HTTP Request Message가 분할 전송될수 있기 때문에 여러번 읽을수 있도록 구현한다.
//    // 2. Body 전용 InputBuffer를 post의 최대 크기로 설정해서 생성하고, HttpInputBuffer의 읽지 않은 버퍼(hasRemaining)를 모두 copy 한다.
//    @Override
//    public int doRead(ByteBuffer buffer) throws IOException {
//        // ByteBuffer가 비어있거나 모든 데이터를 읽은 경우
//
//
//        if (!buffer.hasRemaining()) { // position >= limit
//            // buffer.limit(): 채워진 버퍼의 개수
//            // buffer.capacity(): 버퍼의 최대 용량
//            int bytesRead = fill(buffer);
//            if (bytesRead == -1) {
//                return -1; // EOF
//            }
//            // 읽은 데이터 크기만큼 기존 limit 증가
//            buffer.limit(buffer.limit() + bytesRead);
//
//        }
//
//        // position >= limit -> BufferUnderflowException (RuntimeException) -> catch 400 response
//        // 1 byte를 읽어서 unsigned byte로 변환 (0-255 범위의 int로 반환)
//        return buffer.get() & 0xFF;
//    }
//
//    private int fill(ByteBuffer buffer) throws IOException {
//        if (buffer.capacity() - buffer.limit() <= 0) {
//            // 클라이언트가 보낸 요청 메세지의 헤더나 바디의 크기 초과
//            throw new IllegalStateException("Request message size exceeds buffer capacity");
//        }
//
//        // ByteBuffer에 설정된 최대 크기가 너무 클경우 애플리케이션 메모리가 급증하기 때문에 DEFAULT_READ_BUFFER_SIZE로 설정한다.
//        int readLimitSize = Math.min(buffer.capacity() - buffer.limit(), DEFAULT_READ_BUFFER_SIZE);
//
//        // BioSocketWrapper.read(byte[] buffer, int off, int len)으로 변경
////        return socketWrapper.read(buffer.array(), buffer.position(), readLimitSize);
//        return inputStream.read(buffer.array(), buffer.position(), readLimitSize);
//    }
//
////    private int fill(ByteBuffer buffer) throws IOException {
////        if (buffer.capacity() - buffer.limit() <= 0) {
////            throw new IOException("Buffer is full");
////            // return 0;
////        }
////
////        // ByteBuffer에 설정된 최대 크기가 너무 클경우 애플리케이션 메모리가 급증하기 때문에 DEFAULT_READ_BUFFER_SIZE로 설정한다.
////        int readLimitSize = Math.min(buffer.capacity() - buffer.limit(), DEFAULT_READ_BUFFER_SIZE);
////
////        // BioSocketWrapper.read(byte[] buffer, int off, int len)으로 변경
////        return inputStream.read(buffer.array(), buffer.position(), readLimitSize);
////    }
//
//
//    // Read Persistent method
////    @Override
////    public int doRead(ByteBuffer buffer) throws IOException {
////        // ByteBuffer가 비어있거나 모든 데이터를 읽은 경우
////        if (!buffer.hasRemaining()) { // position >= limit
////            // ByteBuffer 초기화
////            buffer.clear();// pos, limit 등
////
////            // ByteBuffer에 설정된 최대 크기가 너무 클경우 애플리케이션 메모리가 급증하기 때문에 DEFAULT_READ_BUFFER_SIZE로 설정한다.
////            int bytesRead = inputStream.read(buffer.array(), 0, DEFAULT_READ_BUFFER_SIZE);
//////            int bytesRead = inputStream.read(buffer.array(), 0, buffer.capacity());
////
////            if (bytesRead == -1) {
////                return -1; // EOF
////            }
////
////            // 읽은 데이터 크기만큼 limit 설정
////            buffer.limit(bytesRead);
////        }
////
////        // position >= limit -> BufferUnderflowException (RuntimeException) -> catch 400 response
////        // 1 byte를 읽어서 unsigned byte로 변환 (0-255 범위의 int로 반환)
////        return buffer.get() & 0xFF;
////    }
//
////    // InputStream 의존성을 없애기 위해 해당 메서드 제거
////    public InputStream getInputStream () {
////        return inputStream;
////    }
//
//    //    @Override
////    public int doRead() throws IOException {
////        if (bufferPosition >= bufferSize) {
////            bufferSize = inputStream.read(buffer);
////            bufferPosition = 0;
////            if (bufferSize == -1) {
////                return -1; // EOF
////            }
////        }
////        return buffer[bufferPosition++] & 0xFF; // 1 byte 읽어서 int 형으로 표현
////    }
////
//}