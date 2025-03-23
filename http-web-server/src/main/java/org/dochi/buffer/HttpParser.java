package org.dochi.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;


public class HttpParser {
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Fe
    private static final int SEPARATOR_SIZE = 1;
    private static final int CRLF_SIZE = 2;

    // EOF: -1
    // Empty CRLF LINE: 0
    // Parse values in CRLF LINE: Over 0
    public static int parseValuesCrlfLine(InputBuffer inputBuffer, ByteBuffer buffer, MessageBytes[] elements, int separator) throws IOException {
        validateNullElements(elements);
        int count = 0;
        int previousByte = -1;
        int currentByte;
        int start = buffer.position();
        while ((currentByte = getFromBuffer(inputBuffer, buffer)) != -1) {
            if (isSeparator(separator, currentByte) && !isExceedElementCount(count, elements.length)) {
                elements[count++].setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                start = buffer.position();
            } else if (skipSpaceSuffixSeparator(separator, previousByte, currentByte)) {
                start++;
            } else if (isCRLF(previousByte, currentByte)) {
                if (count <= 0) {
                    return 0;
                }
                elements[count++].setBytes(buffer.array(), start, buffer.position() - start - CRLF_SIZE);
                return count;
            }
            previousByte = currentByte;
        }
        return -1;
    }

    // return -1: could not get data in buffer
    // 1. EOF
    // 2. buffer.capacity() - buffer.limit(), len is 0
    private static int getFromBuffer(InputBuffer inputBuffer, ByteBuffer buffer) throws IOException {
        // doRead == 0 이면, len == 0 일때이다.
        // len == 0 값이 들어올경우

        // 버퍼에 남은 데이터가 없을때 + 버퍼링
        // 이때 버퍼링한 값이 -1이면, EOF 이므로 그대로 -1 반환
        // 버퍼링한 값이 0 이면, 버퍼에 남은 데이터도 없으므로 무엇을 반환해야하나. -1을 반환하면 더이상 읽을 값이 없단 의미이다.,
        if (!buffer.hasRemaining() && inputBuffer.doRead(buffer) <= 0) {
            return -1;
        }
        return buffer.get() & 0xFF;
    }

    private static boolean skipSpaceSuffixSeparator(int separator, int previousByte, int currentByte) {
        return isSeparator(separator, previousByte) && currentByte == ' ';
    }

    private static void validateNullElements(MessageBytes[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Elements array cannot be null");
        }

        for (MessageBytes element : elements) {
            if (element == null) {
                throw new IllegalArgumentException("Elements array elements cannot be null");
            }
        }
    }

    private static boolean isSeparator(int separator, int currentByte) {
        return currentByte == separator;
    }

    private static boolean isExceedElementCount(int elementCurrentCount, int elementMaxCount) {
        return elementCurrentCount + 1 >= elementMaxCount;
    }

    private static boolean isCRLF(int prevByte, int currByte) {
        return prevByte == CR && currByte == LF;
    }
}
