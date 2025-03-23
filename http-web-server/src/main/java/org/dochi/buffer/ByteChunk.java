package org.dochi.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ByteChunk {
    public static final Charset DEFAULT_CHARSET;
    private Charset charset;
    private byte[] buffer;
    private int start = 0;
    private int end = 0;

    public ByteChunk() {
        charset = DEFAULT_CHARSET;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    }

    public void setBytes(byte[] b, int off, int len) {
        this.buffer = b;
        this.start = off;
        this.end = this.start + len;
    }

    public void setCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null");
        }
        this.charset = charset;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public int getLength() {
        return this.end - this.start;
    }

    public void recycle() {
        this.start = 0;
        this.end = 0;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public int getStart() {
        return this.start;
    }

//    public void setStart(int start) {
//        if (this.end < start) {
//            this.end = start;
//        }
//        this.start = start;
//    }

    public int getEnd() {
        return this.end;
    }

//    public void setEnd(int end) {
//        this.end = end;
//    }

    public String toString() {
//        CharBuffer cb = this.getCharset().decode(ByteBuffer.wrap(this.buffer, this.start, this.end - this.start));
//        return new String(cb.array(), cb.arrayOffset(), cb.length());

        return new String(buffer, start, end - start, charset);
    }

    public boolean equalsIgnoreCase(String s) {
        int len = end - start;
        if (buffer == null || s == null || len != s.length()) {
            return false;
        }

        int off = start;
        for (int i = 0; i < len; i++) {
            int b = buffer[off++] & 0xFF;
            char c = s.charAt(i);

            // 대문자일 경우 소문자로 변환
            if (b >= 'A' && b <= 'Z') {
                b += 32;
            }
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }

            if (b != c) {
                return false;
            }
        }

        return true;
    }
}
