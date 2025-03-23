package org.dochi.buffer;//package org.dochi.buffer;
//
//public class ByteBuffer {
//    private final byte[] buffer;
//    private int position = 0;
//    private int actualBufferSize = 0;
//
//    public ByteBuffer(int bufferSize) {
//        this.buffer = new byte[bufferSize];
//    }
//
//    public int get() {
//        if (position >= actualBufferSize) {
//            throw new ArrayIndexOutOfBoundsException();
//        }
//        return buffer[position++] & 0xFF;
//    }
//
//    public int getLimitSize() {
//        return buffer.length;
//    }
//
//    public int getActualBufferSize() {
//        return actualBufferSize;
//    }
//
//    public void setActualBufferSize(int actualBufferSize) {
//        this.actualBufferSize = actualBufferSize;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//}
