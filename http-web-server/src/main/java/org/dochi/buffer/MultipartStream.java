//package org.dochi.buffer;
//
//import org.dochi.http.exception.HttpStatusException;
//import org.dochi.http.monitor.MessageSizeMonitor;
//import org.dochi.http.request.multipart.MultipartHeaders;
//import org.dochi.http.request.stream.Http11RequestStream;
//import org.dochi.http.response.HttpStatus;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.BufferOverflowException;
//import java.nio.charset.StandardCharsets;
//
//public class MultipartStream {
//    private final InputStream in;
////    private byte[] buffer;
//
//    // Body: Buffer를 사용한 FileInputStream을 통해 읽는 즉시 write, write한 최대 크기를 벗어나면 예외처리
//    // 헤더를 하나씩 읽어서 :_나 : 기준으로 키 값 분리 후, String으로 저장 (헤더 파라메터는 connecotr.request에서 파싱)
//    // 바디를 하나씩 읽는 버퍼에 모았다가 파일로 write (write한 최대 크기를 벗어나면 예외처리)
//
//    public MultipartStream(InputStream in) {
//        this.in = in;
//    }
//
//    // readHeaders
//    // 매개변수로 받은 MultipartHeaders에 key, value를 분리해서 저장
//    // 헤더를 하나씩 읽어서 :_나 : 기준으로 key와 value 분리 후, MultipartHeaders의 Hashmap에 저장
//    // getPart 메서드에서 헤더와 바디 모두 파싱
//
////    Content-Disposition: form-data; name="metadata"; filename="meta.json"
////    Content-Type: application/json; charset=UTF-8
////    Content-Transfer-Encoding: 8bit
////    Content-Language: en-US
////    Content-ID: <meta123@example.com>
//
//    // CRLF 단위로 헤더를 읽어서 Stirng으로 변환하면 안된다. 헤더의 길이 만큼 한번더 읽어야한다.
//    // 따라서 하나의 바이트씩 읽어서 헤더 파싱
//
//    // 처음부터 다시 멀티파트 로직을 작성하는 것은 오래걸릴것으로 예상
//    // 결단을 해야한다.
//    // 기존의 멀티파트 파싱 로직에 size monitor만 제거해서 구축한다.
//    // 헤더를 읽을때 바이트 단위로 하나씩 읽어야 최대 용량에 대한 오버플로우를 즉각 대응가능하다.
//    // 기존 멀티파트 로직에는 \r\n 단위로 읽고 String으롭 변환했기 때문에 Size Monitor를 주입하였다.,
//    // 만일, 헤더를 읽다가 최대 용량을 초과하면 어떤 false 값을 반환한다. (헤더 용량의 최대값과 멀티 파트 입력스트림을 매개변수로 받는다.)
//
//    // 최대한 기존 프로세싱 활용, size monitoring 제거?
//    // 외부 객체에서 MultipartStream으로 byte 단위로 하나씩 읽어도 사이즈에 대한 트래킹 필요
//    // 모니터링해서 예외를 발생시키는 것이 아니라 읽기 상태값을 반환해서 외부 객체에서 처리하여도된다. 그러나 MultipartStream에서 헤더를 라인단위가 아니라 전부 읽어야한다.
//
//    // tomcat은 섹션의 헤더를 모두 읽다가 최대 크기를 초과하면 IOException 자식 예외 발생
//    // 섹션의 헤더를 모두 읽은 뒤, 파싱을 수행
//    // CRLF 단위로 읽던, 헤더 전체를 읽던 시간복잡도는 2 * n으로 동일
//    // 그러면 헤더 전체를 읽으면 size monitor는 필요없음
//    public String readHeaders(MultipartHeaders headers) throws IOException {
//
//
//    }
//
//
//    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//
//    // Multipart Parser
//    // public parse part
//    // private parse boundary (pure boundary/end boundary)
//    // private parse headers
//    // private parse body
//
//
//
//
//    private static class BoundaryValidator {
//        private static final String BOUNDARY_PREFIX = "--";
//        private static final String CRLF = "\r\n";
//        private static final int MAX_LENGTH = 70; // Boundary 최대 길이
//
//        private byte[] boundary = null;
//        private byte[] endBoundary = null;
//
//        public void validateBoundary(String boundaryValue) {
//            if (isValid(boundaryValue)) {
//                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid multipart/form-data boundary value: " + boundaryValue);
//            }
//            this.boundary = (BOUNDARY_PREFIX + boundaryValue).getBytes(StandardCharsets.US_ASCII);
//            this.endBoundary = (BOUNDARY_PREFIX + boundaryValue + BOUNDARY_PREFIX).getBytes(StandardCharsets.US_ASCII);
//        }
//
//        public boolean isBoundary(byte[] line) {
//            return isMatchBoundary(line, boundary);
//        }
//
//        public boolean isEndBoundary(byte[] line) {
//            return isMatchBoundary(line, endBoundary);
//        }
//
//        private boolean isMatchBoundary(byte[] line, byte[] boundary) {
//            if (line == null || line.length != boundary.length) return false;
//            for (int i = 0; i < line.length; i++) {
//                if (line[i] != boundary[i]) {
//                    return false;
//                }
//            }
//            return true;
//        }
//
//        // RFC 2046
//        private boolean isValid(String boundary) {
//            if (boundary == null || boundary.isEmpty()) {
//                return false;
//            }
//
//            if (isPrefixBoundaryValid(boundary)) {
//                return false;
//            }
//
//            return boundary.length() <= MAX_LENGTH &&
//                    !boundary.contains(CRLF);
//        }
//
//        private boolean isPrefixBoundaryValid(String boundary) {
//            // --와 동일하거나, -- 다음 문자로 -가 아닌 문자가 오면 안됨
//            if (boundary.startsWith(BOUNDARY_PREFIX) && boundary.length() > 2) {
//                // -- + -
//                return boundary.charAt(2) == BOUNDARY_PREFIX.charAt(0);
//            }
//            return !boundary.equals(BOUNDARY_PREFIX);
//        }
//    }
//
////    private static final String BOUNDARY_PREFIX = "--";
//
//    private static final int[] BOUNDARY_PREFIX = {'-', '-'};
//    private static final int[] CRLF = {'\r', '\n'};
//
//
//    // output
//    // max 70 이하
//    // 1. is boundary: --boundaryParamValue
//    // 2. is end boundary: --boundararamValue--
//    // 3. is not boundary: 읽은 값이 70 초과
//
//    // optional
//    // 매개변수 boundaryParamValue
//    // 매개변수 --boundaryValue
//    // 매개변수 --boundaryValue--
//
//
//    // eof: -1
//    // is not boundary: 0
//    // is boundary: 1
//    // is end boundary: 2
//
//    // 해당 메서드 호출하기 위해 boundaryParamLen을 인자로 넘기기전에 70이하인지 확인 필요
//    // boundary 혹은 body를  동시에 처리해야한다. (body가 없을수도 있기 때문에)
//    public BoundaryParseStatus parseBoundary(String boundaryParamValue) throws IOException {
//        if (boundaryParamValue == null || boundaryParamValue.length() > 70) {
//            return BoundaryParseStatus.IS_NOT_BOUNDARY;
//        }
//        int n = 0;
//        int previousByte = -1;
//        int currentByte;
//        boolean endBoundaryReady = false;
//        int boundaryParamLen = boundaryParamValue.getBytes(StandardCharsets.US_ASCII).length;
//        while ((currentByte = in.read()) != -1) {
//            n++;
//            if (n > 2 && n < boundaryParamLen + 2) {
//                if (currentByte != boundaryParamValue.charAt(n-2)) {
//                    return BoundaryParseStatus.IS_NOT_BOUNDARY;
//                }
//            } else if (n == 2 && !(previousByte == '-' && currentByte == '-')) {
//                return BoundaryParseStatus.IS_NOT_BOUNDARY;
//            } else if (n >= boundaryParamLen + 2 && n <= boundaryParamLen + 3) {
//                if (previousByte == '\r' && currentByte == '\n') {
//                    return BoundaryParseStatus.IS_BOUNDARY;
//                } else if (previousByte == '-' && currentByte == '-') {
//                    endBoundaryReady = true;
//                } else {
//                    return BoundaryParseStatus.IS_NOT_BOUNDARY;
//                }
//            } else if (n >= boundaryParamLen + 4) {
//                if (endBoundaryReady && previousByte == '\r' && currentByte == '\n') {
//                    return BoundaryParseStatus.IS_END_BOUNDARY;
//                }
//                return BoundaryParseStatus.IS_NOT_BOUNDARY;
//            }
//            previousByte = currentByte;
//        }
//        return BoundaryParseStatus.EOF;
//    }
//
//
//
//    // 입력스트림으로 읽어서 String으로 반환해서 처리하면, 헤더의 길이 크기만큼 시간복잡도가 증가한다. (Multipart header size * 2)
//    // 파트가 여러개일 경우 시간이 더 걸린다.
//
//    // 읽은 헤더의 크기를 반환해야한다.
//    // 그런데 HeaderParseStatus을 반환하면 크기를 알수 없다.
//    // 현재 헤더를 key, value 로 바로 파싱하기 때문에 헤더의 파싱 상태를 반환한다.
//    // 단, 상태값과 헤더의 크기를 반환하는 것은 비용이 많이 발생한다. 대신 사이즈 모니터링 객체를 주입한다면 코드가 복잡해진다.
//
//    // 헤더 이름의 길이의 크지 않지만, 웹서버의 성능은 클라이언트의 수에 따라 n^2으로 커질수 있다.
//
//
//    // MultipartHeaders 객체룰 MultipartProcessor에서 가져온다.
//    public int parseHeaders(MultipartHeaders headers, int maxHeaderSize) throws IOException {
//        //
//
//
//
////        int n = 0;
//        // 모든 헤더 더해서 반환
//        int totoal = 0;
//        while (true) {
//            int n = parseHeaders(headers, maxHeaderSize);
//            if (n > 0) {
//                totoal += n;
//                if (n == 2) {
//                    return totoal;
//                }
//            } else if (n == -1) {
//                return -1;
//            } else if (n == -2) {
//                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid multipart/form-data format");
//            }
//        }
//    }
//
//
//
//    // 0 반환받으면, header done;
//    private int parseHeader(MultipartHeaders headers, int maxHeaderSize) throws IOException {
//        buffer.reset();
//        int n = 0;
//        int previousByte = -1;
//        int currentByte;
//        boolean isCreateHeader = false;
//        int headerNameIdx = 0;
//        int headerValueIdx = 0;
//        while ((currentByte = in.read()) != -1) {
//            buffer.write(currentByte);
//            n++;
//            if (maxHeaderSize < n) {
//                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "multipart/form-data header exceed");
//            }
//
//            if (currentByte == ':') {
//                validateHeader(isCreateHeader);
//                headerNameIdx = n;
//                headerValueIdx = n + 1;
//                isCreateHeader = true;
//            } else if (previousByte == ':' && currentByte == ' ') {
//                headerValueIdx++;
//            } else if (previousByte == '\r' && currentByte == '\n') {
//                if (n > 2) {
//                    if (isCreateHeader) {
//                        String headerName = new String(buffer.toByteArray(), 0, headerNameIdx, StandardCharsets.US_ASCII);
//                        String headerValue = new String(buffer.toByteArray(), headerValueIdx, n - 2, StandardCharsets.UTF_8);
//                        headers.addHeader(headerName, headerValue);
//                        return n; // HeaderParseStatus.NEED_MORE;
//                    }
//                    return -2; // is not header format
//                }
//                return 2; // HeaderParseStatus.DONE;
//            }
//            previousByte = currentByte;
//        }
//        return -1; // HeaderParseStatus.EOF;
//    }
//
//
//    private int parseBody(MultipartHeaders headers, int maxBodySize) throws IOException {
//        buffer.reset();
//        int n = 0;
//        int previousByte = -1;
//        int currentByte;
//        boolean isCreateHeader = false;
//        int headerNameIdx = 0;
//        int headerValueIdx = 0;
//        while ((currentByte = in.read()) != -1) {
//            buffer.write(currentByte);
//            n++;
//            if (maxBodySize < n) {
//                throw new HttpStatusException(HttpStatus.BAD_REQUEST, "multipart/form-data header exceed");
//            }
//             if (previousByte == '\r' && currentByte == '\n') {
//                // boudnary 인지 검증
//            }
//            previousByte = currentByte;
//        }
//        return -1; // HeaderParseStatus.EOF;
//    }
//
//    // body는 파일(file name 헤더 존재)이라면 바로 바이트 단위로 하나씩 파일에 저장하고 아니라면 텍스트에 저장한다.
//    private void validateHeader(boolean isCreateHeader) {
//        if (isCreateHeader) {
//            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid header field");
//        }
//    }
//
//    public static enum BoundaryParseStatus {
//        IS_BOUNDARY,
//        IS_NOT_BOUNDARY,
//        IS_END_BOUNDARY,
//        OVER_BOUNDARY_PARAMETER_VALUE,
//        EOF;
//
//        private BoundaryParseStatus() {
//        }
//    }
//
//    // 바디를 하나씩 읽는다.
//    // 파일이 아닌 경우에는 힙영역에 저장한다.
//    // 파일인 경우에 버퍼에 저장하고 버퍼가 꽉차면 시스텤 콜을 호출해 파일에 저장한다.
//    // readBody
//
//
//
////    public int readCrlfLine(byte[] b, int off, int len) throws IOException {
////        if (len <= 0) {
////            return 0;
////        }
////        int count = 0;
////        int cur;
////        int prev = -1;
////        while((cur = this.in.read()) != -1) {
////            b[off++] = (byte)cur;
////            ++count;
////            if (prev == '\r' && cur == '\n') {
////                // remove crlf
////                b[off-1] = 0;
////                b[off-2] = 0;
////                return count; // \r\n 포함/미포함 결정
////            } else if (count == len) {
////                throw new BufferOverflowException();
////            }
////            prev = cur;
////        }
////        return -1;
////    }
//}
