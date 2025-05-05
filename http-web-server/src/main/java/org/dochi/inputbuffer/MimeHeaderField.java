package org.dochi.inputbuffer;

public class MimeHeaderField {

    // Header field를 hashmap으로 사용해서 O(1)로 데이터에 접근해야 성능이 빨라진다. InputBuffer를 사용했을땐 byte 배열을 탐색하면서 검색했다.
    // ByteBuffer의 Slice로 인스턴스를 뽑아낼수 있는지 확인 -> 객체의 메모리 주소 값으로 해쉬맵에서 탐색가능한지?

    private final MessageBytes nameMB = MessageBytes.newInstance();
    private final MessageBytes valueMB = MessageBytes.newInstance();

    public MessageBytes getName() {
        return nameMB;
    }

    public MessageBytes getValue() {
        return valueMB;
    }

    public void recycle() {
        this.nameMB.recycle();
        this.valueMB.recycle();
    }

    public String toString() {
        return String.valueOf(this.nameMB) + ": " + String.valueOf(this.valueMB);
    }
}
