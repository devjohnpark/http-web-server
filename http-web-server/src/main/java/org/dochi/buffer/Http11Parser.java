package org.dochi.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;


public class Http11Parser extends HttpParser {
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
        while ((currentByte = readByte(inputBuffer, buffer)) != -1) {
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

    private static boolean skipSpaceSuffixSeparator(int separator, int previousByte, int currentByte) {
        return isSeparator(separator, previousByte) && currentByte == ' ';
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

    // Adapter가 service(data.request, data.response)에서 request로 request-path를 확인해서 http api handler 로드하고 매칭
    // 하지만 request.requestUri의 파싱이 필요하다. utf-8로 문자열로 인코딩후에 ? 기준으로 request-path와 query으로 파싱
    // & 기준으로 parameters 나눈 후, = 기준으로 key와 value로 파싱

    // Adapter의 service에서 Connetor 객체로 connector.Request/Response를 생성해서 필드로 저장
    // (Adapter는 Connector 객체를 참조하며, Connector는 connector.Request/Response를 생성해서 필드로 저장하고 넘겨준다. (필드에 저장값 없으면 생성)
    // connector.Request/Response에 data.Request/Response 객체를 주입
    // connector.Request에서 data.Request의 MessageBytes request-uri을 utf-8로 문자 인코딩해서 String으로 변환후 파싱해서 저장
    // ? 기준으로 request-path와 query-string으로 파싱
    // & 기준으로 parameters 나눈 후 = 기준으로 key와 value로 파싱해서 HashMap<String, String> parameter에 저장
    // Container의 invoke(connector.Request.path())를 호출해서 클래스를 로드한다.
    // 이후 Adapter의 service(data.Request/Response)에서 HttpApiHanler.service(connector.Request/Response) 호출

    // Adapter의 service에서 connector.Request/Response를 생성해서 필드로 저장
    // connector.Request/Response에 data.Request/Response 객체를 주입
    // Container의 invoke(connector.Request, connector.Response)를 호출해서 클래스를 로드한다.
    // Adapter의 connector.Request에 request.requestUri의 파싱이

    // Container의 invoke(connector.Request.path())를 호출해서 클래스를 로드한다.
    // connector.Request의 생성자에서 바이트 단위의 MessageBytes request-uri을 String으로 변환후 파싱
    // connector.Request에 Parameters 저장
    // ? 기준으로 request-path와 query-string으로 파싱
    // & 기준으로 parameters 나눈 후 = 기준으로 key와 value로 파싱
}
