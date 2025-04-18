//package org.dochi.http.api;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//
//public class InputBuffer {
//    private final ByteBuffer buffer; // stream용 버퍼
//
//    // socket buffering to application buffer
//    private final org.dochi.internal.InputBuffer inputBuffer;
//
//    public InputBuffer(int bufferSize, org.dochi.internal.InputBuffer inputBuffer) {
//        this.buffer = initBuffer(bufferSize);
//        this.inputBuffer = inputBuffer;
//    }
//
//    private ByteBuffer initBuffer(int maxSize) {
//        ByteBuffer buffer = ByteBuffer.allocate(maxSize);
//        buffer.flip();
//        return buffer;
//    }
//
//    // read
//    // socketInputBuffer.doRead(ByteBuffer)
//
//    // InputStream -> read()/read(byte[] buf, int off, int len)/read(byte[] buf) -> InputBuffer -> socketInputBuffer.doRead(ByteBuffer)
//    // read(byte[] buf)는 버퍼의 크기만큼 읽기
//
//    public int read() throws IOException {
////        return inputBuffer.doFill(buffer);
//        if (!buffer.hasRemaining()) {
//            inputBuffer.doRead(buffer);
//        }
//        return buffer.get() & 0xFF;
//    }
//
//    // inputBuffer를 통해 읽은 데이터를 넘겨야됨
//    // inputBuffer에서 프로토콜별로 읽어드린 후 스트림으로 반환
//    // 버퍼에 채울 크기: len - off
//    // 현재 버퍼링된 크기: buffer.remaining()
//
//    // stream 버퍼이므로, 순서 그대로 전달하면됨
//    public int read(byte[] buf, int off, int len) throws IOException {
//        // doFill()
//
//
//        int bytesToTransfer = len - off;
//        int remaining = buffer.remaining();
////        int b;
//        if (bytesToTransfer <= buffer.remaining()) {
//            this.buffer.get(buf, buffer.position(), bytesToTransfer);
//            return bytesToTransfer;
//        }
//        // 다음은 inputBuffer.doRead(buffer) 메서드로 버퍼링을 수행
//        // 그런데 버퍼링한 크기를 모른다.
//        //
//
////        // limit - position < len - off
////        int n = 0;
////        while ((b = inputBuffer.doRead(buffer)) && n < ) {
////            this.buffer.get(buf, buffer.position(), bytesToTransfer);
////        }
//
//
////        while ((b = inputBuffer.doRead(buffer)) >= 0 && bytesToTransfer < n) {
////            n+=b;
////        }
////        return n;
//    }
//}
