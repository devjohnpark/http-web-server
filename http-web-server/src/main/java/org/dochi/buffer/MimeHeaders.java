package org.dochi.buffer;

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

    // Header field를 hashmap으로 사용해서 O(1)로 데이터에 접근해야 성능이 빨라진다. InputBuffer를 사용했을땐 byte 배열을 탐색하면서 검색했다.
    // ByteBuffer의 slice()로 인스턴스를 뽑아낼수 있는지 확인 -> 객체의 메모리 주소 값으로 해쉬맵에서 탐색가능한지? 가능할듯 slice(): position ~ limit 영역만 새 버퍼로 생성, 원본과 공유
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
