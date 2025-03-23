package org.dochi.buffer;//package org.dochi.inputbuffer;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;


public class Http11InputBuffer implements InputBuffer {
    private static final Logger log = LoggerFactory.getLogger(Http11InputBuffer.class);
    private static final int CR = '\r';  // Carriage Return
    private static final int LF = '\n';  // Line Fe
    private static final int SEPARATOR_SIZE = 1;
    private static final int CRLF_SIZE = 2;

    private final Request request;
    private final ByteBuffer buffer;
//    private final SocketInputBuffer socketInputBuffer;
    private SocketWrapperBase<?> socketWrapper; // Http11InputBuffer Pool를 위해 변수로 선언
    private final ParseRequestLine parseRequestLine;
    private final ParseHeaderField parseHeaderField;

    public Http11InputBuffer(Request request, int headerMaxSize) {
        this.request = request;
//        this.socketInputBuffer = new SocketInputBuffer();
        this.buffer = initBuffer(headerMaxSize);
        this.parseRequestLine = new ParseRequestLine(request);
        this.parseHeaderField = new ParseHeaderField();
    }

    public ByteBuffer initBuffer(int maxSize) {
        ByteBuffer buffer = ByteBuffer.allocate(maxSize);
        buffer.flip();
        return buffer;
    }

    // Http11InputBuffer는 SocketWrapper는 서버소켓을 사용해서
    public void init(SocketWrapperBase<?> socketWrapper) {
//        this.socketInputBuffer.init(socketWrapper);
        this.socketWrapper = socketWrapper;
    }

    // header를 모두 파싱할때 까지 buffer를 반복적으로 채워야한다. (HTTP/1.0에서 메세지는 분할 전송될수 있기 때문이다.)
    // headerMaxSize까지 버퍼를 채울수 있고 초과하면 400 응답

    // 1. 아래의 로직 EOF 도달전까지 반복
    // 2. 소켓으로부터 버퍼에 바이트를 채우고 실제 읽은 버퍼의 크기를 업데이트
    // 3. \r\n 단위로 시작, 끝 인덱스와 버퍼 메모리 주소를 참조하여, Request에 파싱할 요청 데이터가 포함된 버퍼의 위치 저장 (문자열 파싱은 get 메서드 호출시 수행)
    // 4. \r\n 단위로 찾다가 빈줄이어서 시작과 끝 인덱스의 차이가 2(\r\n)라면 헤더 파싱 종료
    // 5. pos가 실제 읽은 버퍼의 크기까지 도달했다면, 버퍼에 데이터 채운다. (Socket: doRead())
    // 6. pos가 버퍼의 최대 크기에 도달했는데, 헤더 파싱 종료되지 못했다면 헤더 크기를 초과하여 400 응답 (HttpInputBuffer: doRead())

    // Body Part (아래의 로직을 제외하곤 header와 동일)
    // 8. body 읽기 필요할때, body의 최대 크기에 맞게 Body용 InputBuffer 생성하고 기존의 Http11InputBuffer의 doRead()를 호출해서 읽어온 남은 데이터를 버퍼에 복사 후 개발자에게 반환할 InputStream에 주입
    // 9. multipart는 파일 최대 사이즈에 맞는 inputbuffer 생성해서하고 파싱처리


//    // 바디(POST, MUTIPART)용 InputBuffer에 SocketInputBuffer 주입해서 생성
//    // body 읽기 필요할때, body의 최대 크기에 맞게 Body용 InputBuffer 생성후, 기존의 Http11InputBuffer의 doRead()를 호출해서 읽어온 남은 데이터를 바디용 버퍼에 저장
//    public InputBuffer getSocketInputBuffer() {
//        return socketInputBuffer;
//    }

//    // HttpApiRequest에 주입할 body 읽기용 버퍼를 초기화할때, 기존의 헤더 읽을때 버퍼링한 body의 데이터도 포함될수 있으니 가져다 복사해야하기 위해 버퍼 넘김
//    public ByteBuffer getByteBuffer() {
//        return this.buffer;
//    }

    // HttpApiRequest에 주입할 body 읽기용 버퍼를 초기화할때, 기존의 헤더 읽을때 버퍼링한 body의 데이터도 포함될수 있으니 가져다 복사해야하기 위해 버퍼 복사
    @Override
    public int duplicate(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalStateException("buffer is null");
        }
        int bytesToTransfer = this.buffer.remaining();
        if (this.buffer.hasRemaining() && bytesToTransfer <= buffer.remaining()) {
//            System.arraycopy(this.buffer.array(), this.buffer.position(),
//                    buffer.array(), buffer.position(),
//                    bytesToTransfer);

            // 실제로 System.arraycopy()와 ByteBuffer의 get() or put()의 속도 차이 비교 필요
//            // 배열 복사를 피하기: System.arraycopy()는 배열 복사를 직접 수행하는 방식이라, ByteBuffer의 API를 사용하면 내부 최적화가 가능하여 더 빠를 수 있다. (비교해봐야함)
//            // 메모리 접근 최적화: ByteBuffer의 get()과 put() 메서드는 내부 메모리 접근을 최적화하여 성능을 개선한다.
            // ByteBuffer의 get() or put(): 네이티브 memcpy() 기반의 블록 복사: 블록 복사(Block Copy)는 메모리에서 특정 크기의 데이터를 한 번에 복사하는 방식 (하나씩 검색하지 않음)
            // CPU의 최적화된 명령어(SIMD, AVX, SSE 등)를 활용 가능, 캐시 최적화 적용 가능 (CPU L1/L2 캐시 활용)
//            Direct ByteBuffer 사용 호환: ByteBuffer의 allocateDirect() 메서드를 사용하면, JVM 힙 메모리가 아닌 운영체제의 네이티브 메모리에 할당되어 입출력 성능을 향상시킬 수 있다.
//            this.buffer.get(buffer.array(), buffer.position(), bytesToTransfer);
//
//            this.buffer.position(this.buffer.position() + bytesToTransfer);

            buffer.put(this.buffer);

            return bytesToTransfer;
        }
        return 0;
    }

    // SocketProcessor.run() -> doRun() -> HttpProcessor를 가져와서 SocketWrapper를 초기화해서 사용
    public void recycle() {
        buffer.position(0);
        buffer.limit(0);
        request.recycle();
//        socketInputBuffer.recycle();
    }

//    // 1. 버퍼의 최대 크기만큼 한번에 읽어서 초기화 (하지만 HTTP Request Message가 분할 전송될수 있기 때문에 여러번 읽어야할수 있다.)
//    // 2. Body 전용 InputBuffer를 post의 최대 크기로 설정해서 생성하고, HttpInputBuffer의 읽지 않은 버퍼(hasRemaining)를 모두 copy 한다.
//    @Override
//    public int doRead(ByteBuffer buffer) throws IOException {
//        int bytesToTransfer = this.buffer.remaining();
//        if (this.buffer.hasRemaining() && bytesToTransfer <= buffer.remaining()) {
////            System.arraycopy(this.buffer.array(), this.buffer.position(),
////                    buffer.array(), buffer.position(),
////                    bytesToTransfer);
//
//              // 실제로 System.arraycopy()와 ByteBuffer의 get() or put()의 속도 차이 비교 필요
////            // 배열 복사를 피하기: System.arraycopy()는 배열 복사를 직접 수행하는 방식이라, ByteBuffer의 API를 사용하면 내부 최적화가 가능하여 더 빠를 수 있다. (비교해봐야함)
////            // 메모리 접근 최적화: ByteBuffer의 get()과 put() 메서드는 내부 메모리 접근을 최적화하여 성능을 개선한다.
//             // ByteBuffer의 get() or put(): 네이티브 memcpy() 기반의 블록 복사: 블록 복사(Block Copy)는 메모리에서 특정 크기의 데이터를 한 번에 복사하는 방식 (하나씩 검색하지 않음)
//            // CPU의 최적화된 명령어(SIMD, AVX, SSE 등)를 활용 가능, 캐시 최적화 적용 가능 (CPU L1/L2 캐시 활용)
////            Direct ByteBuffer 사용 호환: ByteBuffer의 allocateDirect() 메서드를 사용하면, JVM 힙 메모리가 아닌 운영체제의 네이티브 메모리에 할당되어 입출력 성능을 향상시킬 수 있다.
////            this.buffer.get(buffer.array(), buffer.position(), bytesToTransfer);
////
////            this.buffer.position(this.buffer.position() + bytesToTransfer);
//
//            buffer.put(this.buffer);
//
//            return bytesToTransfer;
//        }
//        return 0;
//    }


    @Override
    public int doRead(ByteBuffer buffer) throws IOException {
        if (buffer == null) {
            throw new IllegalStateException("buffer is null");
        }
        int bufferingLimitSize = buffer.capacity() - buffer.limit();
        if (bufferingLimitSize <= 0) {
            // 클라이언트가 보낸 요청 메세지의 헤더나 바디의 크기 초과
            throw new IllegalStateException("Request message size exceeds buffer capacity");
        }
        if (this.socketWrapper == null) {
            throw new IllegalStateException("No socket wrapper is initialized");
        }
        int bytesRead = this.socketWrapper.read(buffer.array(), buffer.position(), bufferingLimitSize);
        if (bytesRead > 0) {
            // 읽은 데이터 크기만큼 기존 limit 증가
            buffer.limit(buffer.limit() + bytesRead);
        }
        return bytesRead;
    }

    public boolean parseHeader() throws IOException {
        return parseRequestLine() && parseHeaders();
    }

//    private boolean parseRequestLine() throws IOException {
//        int count = parseValuesCRLF(parseRequestLine.getRequestLine(), ' ');
//        if (count == -1) {
//            return false;
//        } else if (count == 3) {
//            return true;
//        }
//        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request line");
//    }

//    private boolean parseRequestLine() throws IOException {
//        int parseValuesCnt = 0;
//        while ((parseValuesCnt = HttpParser.parseValuesInCRLF(buffer, parseRequestLine.getRequestLine(), ' ')) != 3) {
//            if (parseValuesCnt == -1) {
//                if (fill(buffer) <= 0) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    private boolean parseRequestLine() throws IOException {
        int parseValuesCnt = HttpParser.parseValuesCrlfLine(this, buffer, parseRequestLine.getRequestLine(), ' ');
        if (parseValuesCnt == 3) return true;
        else if (parseValuesCnt == -1) return false;
        else throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request line");
    }

    private static boolean validateRequestLine(int parseValuesCnt) {
        return parseValuesCnt == 3;
    }

//    private boolean parseHeaders() throws IOException {
//        MimeHeaderField headerField = request.headers().createHeader();
//        parseHeaderField.setHeaderField(headerField);
//        int parseValuesCnt = 0;
//        while ((parseValuesCnt = parseValuesCRLF(parseHeaderField.getHeaderField(), ':')) != -1) {
//            if (isBlankLine(parseValuesCnt)) return true;
//            validHeaderFieldName(headerField);
//            headerField = request.headers().createHeader();
//            parseHeaderField.setHeaderField(headerField);
//        }
//        return false;
//    }

    private boolean parseHeaders() throws IOException {
        MimeHeaderField headerField = request.headers().createHeader();
        parseHeaderField.setHeaderField(headerField);
        int parseValuesCnt = 0;
        while ((parseValuesCnt = HttpParser.parseValuesCrlfLine(this, buffer, parseHeaderField.getHeaderField(), ':')) != -1) {
            if (isBlankLine(parseValuesCnt)) return true;
            else if (parseValuesCnt > 2) throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request header");
            validHeaderFieldName(headerField);
            headerField = request.headers().createHeader();
            parseHeaderField.setHeaderField(headerField);
        }
        return false;
    }

    private boolean isBlankLine(int parseValuesCnt) {
        if (parseValuesCnt == 0) {
            request.headers().removeHeader(); // remove MessageBytes when blank line
            return true;
        }
        return false;
    }

    private void validHeaderFieldName(MimeHeaderField headerField) {
        if (headerField.getName().getLength() == 0) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "HTTP Header field name must not be empty");
        }
    }

    // Http11InputBuffer에서 읽으면서 파싱을 수행하는 이유?
    // HTTP 요청 메시지를 처리하려면 소켓에서 데이터를 읽고, 이를 해석해야 한다.
    // HttpInputBuffer는 소켓에서 데이터를 읽고 이를 내부 버퍼에 저장하는 역할(버퍼링)을 하며, 단순한 버퍼링 역할을 넘어서, 기본적인 파싱 작업도 수행한다.
    // HTTP 요청을 효율적으로 처리하기 위해 '읽기(Read) + 파싱(Parse)'를 Http11InputBuffer 한 곳에서 처리
    // 결론, HttpInputBuffer에서 읽기와 파싱을 함께 수행하도록 설계해서 성능 최적화
    // 하지만, Http11InputBuffer의 내부적으로는 실제 데이터를 읽는 작업을 SocketInputBuffer에게 위임해서 단일 책임을 갖도록 한다.
    // 따라서, Http11InputBuffer가 소켓 입출력 세부사항에 의존하지 않도록 분리하여, SocketInputBuffer은 향후 다른 네트워크 계층 (ex. SSL, HTTP/2)로 확장 가능하다.
    // 또한, POST 데이터를 읽어 처리하는 InputBuffer나 상대적으론 큰 파일을 multipart/form-data 데이터를 처리하는 InputBuffer 구현체에 SocketInputBuffer를 주입할수 있다.
    // 이렇게 네트워크 I/O(SocketInputBuffer)와 HTTP 메시지 파싱(Http11InputBuffer)을 분리하여 유지보수성을 높이진다.

    // 상세1: 만일, 읽기와 파싱을 분리한다면?
    // 읽기와 파싱을 분리하면 성능 저하 가능성이 있음
    // 만약 HttpInputBuffer가 단순한 "버퍼 역할"만 하고, HttpParser가 모든 파싱을 담당한다면?
    // 읽기(Read) 단계에서 한 번 데이터를 메모리에 저장
    // 이후 파싱(Parsing) 단계에서 다시 데이터를 처리
    // 결과적으로 불필요한 메모리 복사와 CPU 연산이 발생
    // 특히, 대량의 HTTP 요청을 처리할 때 성능이 저하될 가능성이 크다.

    // 상세2: Http11InputBuffer의 내부
    // SocketInputBuffer는 Http11InputBuffer 내부에서 소켓에서 직접 데이터를 읽고 버퍼링하는 역할을 수행
    // 소켓에서 데이터를 읽어들여 Http11InputBuffer에서 주입한 ByteBuffer로 버퍼링을 한다.
    // Http11InputBuffer에 주입시 소켓에서 데이터를 읽는 객체의 호환서을 위해 InputBuffer을 구현한 SocketInputBuffer을 주입
    // SocketInputBuffer의 내부에서는 여러 소켓 입력에 대한 방법을 선택할수 있다. Ex) SocketInputStream/SocketChannel
    // SocketChannel을 사용하면 InputStream 없이 소켓에서 데이터를 읽을 수 있다. (SocketChannel.open(): 블로킹/논블로킹 모드로 소켓 연결 가능)
    // 따라서, SocketInputBuffer에서 InputStream를 주입하는 것이 아니라, 호환되도록 추상 클래스 SocketWrapper를 주입받고 BioSocketWrapper 상속받은 객체를 주입한다.

    // Http11InputBuffer에서 SocketInputBuffer을 생성
    // InputBuffer 생성해서 주입해서 또 다른 InputBuffer를 생성한다면 논리적으로 말이 안된다.
    // 따라서 Http11InputBuffer 내부에서 SocketInputBuffer을 생성하도록한다.
    // AbstractHttpProcessor 구현체로부터 Http11InputBuffer은 SocketWrapper를 주입받아 SocketInputBuffer에 주입시켜서 생성한다. -> SocketInputBuffer(BioSocketWrapper)
    // 단, SocketWrapper가 읽기 기능을 수행할수 있어야아한다.
    // 추상 클래스 SocketWrapperBase<E socket>를 생성하고 SocketWrapperBase<Socket socket> extends SocketWrapperBase 로 그대로 처리.
    // SocketWrapperBase<E socket>: read(ByteBuffer buffer)
    // BioSocketWrapper<Socket socket>: read socket.getInputStream.read(buffer.array(), buffer.position, buffer.limit())

    // Http11InputBuffer의 테스트: InputStream을 어떻게 주입할 것인가?
    // Http11InputBuffer 객체에 요청 메세지로 초기화된 InputStream을 연속적으로 주입할수 있으면 중복 코드를 작성하지 않아도되서 테스트 코드 작성이 수월하다.
    // 그러나 InputStream을 감싸서 주입해야 여러 소켓 입력 방식에 의존성이 낮아지지만, 테스트 코드에서 요청 메세지를 적재한 InputStream 주입이 안된다.
    // BioSocketWrapper.setInputStream(InputStream)은 따로 작성되지 않았기 때문이다.
    // 그렇다면 답은 실제 소켓을 사용해서 메세지를 주고 받는것이다 (클라 출력 -> 서버 입력), 하지만 스레드 클라, 서버 각각 생성해서 지속 실행시켜야한다. -> 테스틐 코드가 상당히 복잡해진다.
    // 일단은 SocketInputBuffer에 InputStream을 주입한다. 그전에 Http11InputBuffer에 setInputStream(InputStream)으로 주입한다. 그러면 Http11InputBuffer 객체를 여러 테스트 메서드에 재활용할수 있다.

    // SocketInputBuffer를 Http11InputBuffer의 외부에서 생성하는 것은 비논리적인가?
    // 일단, SocketInputBuffer은 HttpXXInputBuffer에 의존성을 띄진 않는다.
    // 그렇다면, AbstractHttpProcessor에서 주입받은 SocketInputStream으로 SocketInputBuffer을 생성하는것은 어떠한가?
    // 그러면, input buffer에 input buffer를 주입하는 것은 논리적인가? 비논리적
    // 그런데 SocketWrapper가 read 작업을 수행하는 것은 적합하다. socket이 read를 수행하기 때문이다.
    // 그렇다면 SocketInputBuffer가 read를 수행하는 것이 아닌 SocketWrapper가 ByteBuffer를 주입받아서 read를 수행하는 것이 적합하다.
    // 허나, 테스트를 하기 위해서는 InputStream을 넣어야한다...
    // 이것을 돌파하기 위해 테스트 코드 작성법 책 읽는다. 두 가지 방법이 있다. (Mock/Unit Test)
    // Unit Test로 서버와 클라 소켓을 사용해서 테스트 가능 옵시디언 Http11inputBuffer에 해결책 적어놈
    // 그런데 SocketInputBuffer 클래스가 필요한지 따져봐야한다. SocketInputBuffer를 또 생성하면, 레이어가 1 게층 더 생기며, 객체 생성 비용이 1개 더 든다.
    // 외부에서 doRead(ByteBuffer) 메서드로 데이터를 InputBuffer의 데이터를 읽어올수 있음 -> Http11InputBuffer는 주입받은 SocketWrapper로 읽어서 버퍼링 수행
    // AbstractProcessor 클래스의 setSocketWrapper 추상 메서드로 Http11Processor에서 Http11InputBuffer.init(BioSocketWrapper)/Http11OutputBuffer.init(BioSocketWrapper)
    // 프로토콜 판별 -> Http11Processor 생성 -> Http11Processor 객체 생성시, Request, HttpApiRequest 객체 초기화
    // HttpXXProcessor 객체를 미리 생성할순 없을까? -> 기본 개수만 생성하고 초과하면 동적으로 2배씩 확장? or 동적으로 하나씩 생성하고 재활용
    // AbstractProtocol 클래스에서 SynchronizedStack 객체를 사용해서 HttpXXProcessor 객체를 push(), pop()
    // AbstractProcessor 클래스도 AbstractProtocol 클래스에서 recycle를 통해 초기화해서 재활용
    // AbstractProcessor을 재활용하기 위해서 해당 클래스에 setSocketWrapper(BioSocketWrapper) 메서드를 정의하였다.
    // 그리고 HttpXXInputBuffer 재홀용하기 위해서 해당 클래스에 init(BioSocketWrapper) 메서드를 정의하였다.
    // 그리고 HttpXXInputBuffer 재홀용하기 위해서 해당 클래스에 init(BioSocketWrapper) 메서드를 정의하였다.
    // 소켓 데이터의 읽기는 SocketWrapper가 책임지므로 SocketInputBuffer의 로직을 Http11inputBuffer에 위임하는 것이 합리적이고 레이어 줄이고 객체 생성비용을 아낀다.


    // Http11InputBuffer 파싱 범위
    // - 요청 라인과 헤더를 파싱하며, 바디 데이터를 읽어 처리하는 것은 HttpServletRequest 이다.
    // - 버퍼에 남은 데이터는 외부에서 doRead 호출해서 바디 최대값으로 초기화된 바디용 버퍼에 복사한다.

//    private int parseValuesCRLF(MessageBytes[] elements, int separator) throws IOException {
//        validateNullElements(elements);
//        int count = 0;
//        int previousByte = -1;
//        int currentByte;
//        int start = buffer.position();
//        while ((currentByte = getByte()) != -1) {
//            if (isSeparator(separator, currentByte) && !isExceedElementCount(count, elements.length)) {
//                elements[count++].setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
//                start = buffer.position();
//            } else if (skipSpaceSuffixSeparator(separator, previousByte, currentByte)) {
//                start++;
//            } else if (isCRLF(previousByte, currentByte)) {  // 마지막 chunk 파싱 (chunk / chunk + \r\n)
//                if (count > 0) {
//                    elements[count++].setBytes(buffer.array(), start, buffer.position() - start - CRLF_SIZE);
//                }
//                return count;
//            }
//            previousByte = currentByte;
//        }
//        return -1;
//    }


//    private int getNextByte() throws IOException {
//        try {
//            return socketInputBuffer.doRead(buffer);
////            if (socketInputBuffer.hasSocket()) {
////                return socketInputBuffer.doRead(buffer);
////            }
////            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No socket available");
//        } catch (IllegalStateException e) {
//            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "HTTP Header exceeded max size");
//        }
//    }

//    private int getByte() throws IOException {
//        if (!buffer.hasRemaining() && fill(buffer) <= 0) {
//            return -1;
//        }
//        return buffer.get() & 0xFF;
//    }
//
//    private int fill(ByteBuffer buffer) throws IOException {
//        return socketInputBuffer.doRead(buffer);
//    }

//    // -1: EOF
//    // 0: fill 할필요 없음 0 반환
//    private int fill(ByteBuffer buffer) throws IOException {
//        if (!buffer.hasRemaining()) {
//            return socketInputBuffer.doRead(buffer);
//        }
//        return 0;
//    }

//    public static class SocketInputBuffer implements InputBuffer {
////        private static final int DEFAULT_READ_BUFFER_SIZE = 8192;
//
//        private SocketWrapperBase<?> socketWrapper;
//
////        private InputStream inputStream;
//
//        public void init(SocketWrapperBase<?> socketWrapper) {
//            this.socketWrapper = socketWrapper;
//        }
//
////        // 테스트 임시
////        public void init(InputStream inputStream) {
////            this.inputStream = inputStream;
////        }
//
//        // SocketProcessor가 SocketWrapper을 reset하고, SocketWrapper는 socket을 감싼다.
//        // 따라서 recycle 한다면, SocketWrapper = null 해도된다.
//
//        // HttpProcessor와 함께 Http11InputBuffer는 recycle() 이후, 다른 클라이언트 요청에 사용하므로 SocketWrapper는 null값으로 설정
//        // SocketProcessor에서 SocketWrapper를 reset해서 사용
//        // SocketProcess.run() -> doRun() -> HttpProcessor를 가져와서 SocketWrapper를 초기화해서 사용
//        public void recycle() {
//            this.socketWrapper = null;
//        }
//
//        // Http11InputBuffer <- private int readByte(): buffer.get() & 0xFF;
//        // SocketInputBuffer <- public int fill(): 버퍼링
//        // doRead: 버퍼링한 length를 반환해야한다.
//
//        // 1. 버퍼에 데이터 안남아있으면, SocketInputBuffer.doRead 호출 (fill)
//        // 2. buffer.get() & 0xFF;
//
//        @Override
//        public int doRead(ByteBuffer buffer) throws IOException {
//            int bufferingLimitSize = buffer.capacity() - buffer.limit();
//            if (bufferingLimitSize <= 0) {
//                // 클라이언트가 보낸 요청 메세지의 헤더나 바디의 크기 초과
//                throw new IllegalStateException("Request message size exceeds buffer capacity");
//            }
//            if (socketWrapper == null) {
//                throw new IllegalStateException("No socket wrapper is initialized");
//            }
//            int bytesRead = socketWrapper.read(buffer.array(), buffer.position(), bufferingLimitSize);
//            if (bytesRead > 0) {
//                // 읽은 데이터 크기만큼 기존 limit 증가
//                buffer.limit(buffer.limit() + bytesRead);
//                return bytesRead;
//            }
//            return -1; // EOF
//        }
//    }

//    private boolean skipSpaceSuffixSeparator(int separator, int previousByte, int currentByte) {
//        return isSeparator(separator, previousByte) && currentByte == ' ';
//    }
//
//    private void validateNullElements(MessageBytes[] elements) {
//        if (elements == null) {
//            throw new IllegalArgumentException("Elements array cannot be null");
//        }
//
//        for (MessageBytes element : elements) {
//            if (element == null) {
//                throw new IllegalArgumentException("Elements array elements cannot be null");
//            }
//        }
//    }

//    private static boolean isSeparator(int separator, int currentByte) {
//        return currentByte == separator;
//    }
//
//    private boolean isExceedElementCount(int elementCurrentCount, int elementMaxCount) {
//        return elementCurrentCount + 1 >= elementMaxCount;
//    }
//
//    private boolean isCRLF(int prevByte, int currByte) {
//        return prevByte == CR && currByte == LF;
//    }

    private static class ParseRequestLine {
        private final MessageBytes[] requestLine = new MessageBytes[3];

        public ParseRequestLine(Request request) {
            this.requestLine[0] = request.method();
            this.requestLine[1] = request.requestURI();
            this.requestLine[2] = request.version();
        }

        public MessageBytes[] getRequestLine() {
            return requestLine;
        }
    }

    private static class ParseHeaderField {
        private final MessageBytes[] headerField = new MessageBytes[2];

        public void setHeaderField(MimeHeaderField headerField) {
            this.headerField[0] = headerField.getName();
            this.headerField[1] = headerField.getValue();
        }

        public MessageBytes[] getHeaderField() {
            return headerField;
        }
    }
}
