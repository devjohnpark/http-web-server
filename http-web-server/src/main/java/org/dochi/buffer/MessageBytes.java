package org.dochi.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class MessageBytes {
    private final ByteChunk byteChunk = new ByteChunk();
    private int type = 0;
    private String strValue;
    private static final MessageBytesFactory factory = new MessageBytesFactory();

    private MessageBytes() {
    }

    public static MessageBytes newInstance() {
        return factory.newInstance();
    }

    public void setBytes(byte[] b, int off, int len) {
        this.byteChunk.setBytes(b, off, len);
        this.type = 1;
    }

    public ByteChunk getByteChunk() {
        return this.byteChunk;
    }

    public void recycle() {
        this.byteChunk.recycle();
        this.strValue = null;
        this.type = 0;
    }


    public int getLength() {
        if (this.type == 1) {
            return this.byteChunk.getLength();
        } else if (this.type == 2) {
            return this.strValue.length();
        } else {
            return 0;
        }
    }

    public String toString() {
        if (this.strValue == null) {
            this.strValue = this.byteChunk.toString();
        }
        return this.strValue;
    }

    public void setString(String str) {
        this.strValue = str;
        if (str != null) {
            this.type = 2;
        }
    }

    public void toByte() {
        if (this.strValue != null) {
            ByteBuffer bb = this.getCharset().encode(this.strValue);
            this.byteChunk.setBytes(bb.array(), bb.arrayOffset(), bb.limit());
        }
    }


    /*
    Request에서 path 혹은 query string에 setCharset() -> MessageBytes
    HeaderField에서 필드 이름은 기본 charset이다.
    HeaderField에서 필드 값의 setCharset() -> MessageBytes
     */
    public void setCharset(Charset charset) {
        this.byteChunk.setCharset(charset);
    }

    public Charset getCharset() {
        return this.byteChunk.getCharset();
    }

    public boolean equalsIgnoreCase(String s) {
        switch (this.type) {
            case 1:
                return this.byteChunk.equalsIgnoreCase(s);
            case 2:
                if (this.strValue == null) {
                    return s == null;
                }
                return this.strValue.equalsIgnoreCase(s);
            default:
                return false;
        }
    }

    private static class MessageBytesFactory {
        protected MessageBytesFactory() {
        }

        public MessageBytes newInstance() {
            return new MessageBytes();
        }
    }
}
