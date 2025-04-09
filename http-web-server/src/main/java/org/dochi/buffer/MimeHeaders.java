package org.dochi.buffer;

import java.util.HashMap;
import java.util.Map;

public class MimeHeaders {
    private static final int DEFAULT_HEADER_FIELD_COUNT = 8;
    private int len = 0;
    private int count = 0;

    private MimeHeaderField[] headers;
    
    public MimeHeaders() {
        initHeaders(DEFAULT_HEADER_FIELD_COUNT);
    }

    public MimeHeaders(int length) {
        initHeaders(length);
    }

    private void initHeaders(int len) {
        this.headers = new MimeHeaderField[len];
        // 배열의 각 요소 초기화
        for (int i = 0; i < this.headers.length; i++) {
            this.headers[i] = new MimeHeaderField();
        }
        this.len = len;
    }

    public void recycle() {
        for(int i = 0; i < this.count; ++i) {
            this.headers[i].recycle();
        }
        this.count = 0;
    }


//    // buf, start index, end index
//    public MimeHeaderField addHeaderField(int index) {
//        if (index >= count && count >= len) {
//            expandHeaders();
//        }
//        return headers[count++];
//    }
    
//    public MessageBytes addValue()
//
//
//    public MimeHeaderField addHeaderField() {
//        if (count >= len) {
//            expandHeaders();
//        }
//        return headers[count++];
//    }

//    public MessageBytes addValue(byte[] buffer, int start, int length) {
//        if (count >= this.len) {
//            int newLength = count * 2;
//            if (this.len > 0 && newLength > this.len) {
//                len = newLength;
//            }
//            MimeHeaderField[] tmp = new MimeHeaderField[this.len];
//            System.arraycopy(headers, 0, tmp, 0, count);
//            headers = tmp;
//        }
//        headers[count].getName().setBytes(buffer, start, length);
//        return headers[count++].getValue();
//    }

    // return last index of headers

    // getHeaderField: 헤더 필드를 가져오기
    // getHeaderField -> name -> setBytes
    // getHeaderField -> value -> setBytes

    // getHeaderCount -> return count
    // int addHeader() -> 만일 헤더 필드의 개수가 꽉차면, 메모리 동적 확장후 return count

    // addName(count): 만일 헤더 필드의 개수가 꽉차면, 메모리 동적 확장 -> return MimeHeader
    // addValue(count): 만일 헤더 필드의 이름

    // int createHeaderField() -> count -> 만일 헤더 필드의 개수가 꽉차면, 메모리 동적 확장후 MimeHeaderField[count] 반환
    // addName(int no) -> setBytes
    // addValue(int no) -> setBytes

    // createHeader

//    public int createHeader() {
//        if (count >= this.len) {
//            int newLength = count * 2;
//            if (this.len > 0 && newLength > this.len) {
//                len = newLength;
//            }
//            MimeHeaderField[] tmp = new MimeHeaderField[this.len];
//            System.arraycopy(headers, 0, tmp, 0, count);
//            headers = tmp;
//        }
//        return count++;
//    }

    public MimeHeaderField createHeader() {
        if (count >= len) {
            int newLength = count * 2;
            if (len > 0 && newLength > len) {
                len = newLength;
            }
            MimeHeaderField[] tmp = new MimeHeaderField[len];
            System.arraycopy(headers, 0, tmp, 0, count);
            headers = tmp;
        }
        return headers[count++];
    }

    public int size() {
        return this.count;
    }

    public void removeHeader() {
        if (count <= 0) {
            return;
        }
        --count;
    }

    public MessageBytes getName(int n) {
        return n >= 0 && n < this.count ? this.headers[n].getName() : null;
    }

    public MessageBytes getValue(int n) {
        return n >= 0 && n < this.count ? this.headers[n].getValue() : null;
    }

//    public void addName(int index, byte[] buffer, int start, int length) {
//        if (count <= index) {
//            throw new IllegalArgumentException("Index parameter greater than the number of generated header fields.");
//        }
//        if (length == 0) {
//            throw new IllegalArgumentException("HTTP Header field name must not be empty.");
//        }
//        headers[index].getName().setBytes(buffer, start, length);
//    }
//
//    public void addValue(int index, byte[] buffer, int start, int length) {
//        if (count <= index) {
//            throw new IllegalArgumentException("Index parameter greater than the number of generated header fields.");
//        }
//        headers[index].getValue().setBytes(buffer, start, length);
//    }


    // 헤더 수가 적은 경우(보통 10~30개 수준)는 배열 순회가 해시보다 빠를 수도 있다.
    // 버퍼에서 헤더는 연손적으로 메모리상에 저장되기 때문에 캐시히트가 높다. (JIT 최적화 좋음)
    // 반면, 해시 맵은 내부적으로 포인터 배열을 사용하고 해시 충돌이 발생했을 경우 Red-Black Tree로 변환해서 log(n)으로 처리 (해시 연산과 해시 충돌에서 오버헤드 발생)
    // 포인터 배열을 사용하므로 메모리가 연속적으로 저장되지 않아서 캐시 히트가 낮을수 있다. (JIT 최적화 낮음)
    // 수많은 클라이언트의 동시 요청을 처리할때 GC까지 고려해서 성능을 비교해야한다.
    public MessageBytes getValue(String name) {
        for(int i = 0; i < this.count; i++) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                return this.headers[i].getValue();
            }
        }
        return null;
    }

    public String getHeader(String name) {
        MessageBytes byteValue = this.getValue(name);
        return byteValue != null ? byteValue.toString() : null;
    }
}
