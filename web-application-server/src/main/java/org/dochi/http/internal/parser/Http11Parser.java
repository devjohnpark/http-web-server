package org.dochi.http.internal.parser;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.data.HttpStatus;
import org.dochi.http.data.raw.MimeHeaderField;
import org.dochi.http.data.raw.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Http11Parser {
    private static final Logger log = LoggerFactory.getLogger(Http11Parser.class);
    private static final int SEPARATOR_SIZE = 1;
    private static final int CRLF_SIZE = 2;
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte WHITE_SPACE = ' ';
    private static final byte TAB = '\t';
    private static final byte QUERY_SP = '?';
    private static final byte HEADER_KEY_VALUE_SP = ':';


    private final HeaderDataSource source;

    public Http11Parser(HeaderDataSource source) {
        this.source = source;
    }

    private byte getByte() throws IOException {
        if (!this.source.getHeaderByteBuffer().hasRemaining() && !this.source.fillHeaderBuffer()) {
            return -1;
        }
        return this.source.getHeaderByteBuffer().get();
    }

    public boolean parseRequestLine(Request request) throws IOException {
        int elementCnt = 0;
        int querySeparator = -1;
        byte previousByte = -1;
        byte currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int start = buffer.position();
        while ((currentByte = getByte()) != -1) {
            if (currentByte == WHITE_SPACE) {
                elementCnt++;
                if (elementCnt == 1) {
                    request.method().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                } else if (elementCnt == 2) { 
                    request.requestURI().setCharset(StandardCharsets.UTF_8);
                    request.requestURI().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                    request.requestPath().setCharset(StandardCharsets.UTF_8);
                    if (querySeparator != -1) {
                        request.requestPath().setBytes(buffer.array(), start, querySeparator - start - SEPARATOR_SIZE);
                        request.queryString().setCharset(StandardCharsets.UTF_8);
                        request.queryString().setBytes(buffer.array(), querySeparator, buffer.position() - querySeparator - SEPARATOR_SIZE);
                    } else {
                        request.requestPath().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                    }
                }
                start = buffer.position();
            } else if (currentByte == QUERY_SP && querySeparator == -1) {
                querySeparator = buffer.position();
            } else if (previousByte == CR && currentByte == LF) {
                if (elementCnt != 2) {
                    throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request line");
                }
                request.protocol().setBytes(buffer.array(), start, buffer.position() - start - CRLF_SIZE);
                return true;
            }
            previousByte = currentByte;
        }
        return false;
    }

    public boolean parseHeaders(Request request) throws IOException {
        HeaderParseStatus status;
        do {
            status = parseHeaderField(request);
        } while (status == HeaderParseStatus.NEED_MORE);
        return status == HeaderParseStatus.DONE && request.headers().size() > 0;
    }

    private HeaderParseStatus parseHeaderField(Request request) throws IOException {
        byte previousByte = -1;
        byte currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int nameStart = buffer.position();
        int nameEnd = nameStart;
        int valueStart = nameStart;
        int valueEnd = nameStart;
        
        while ((currentByte = getByte()) != -1) { // 1 2
            if (currentByte == HEADER_KEY_VALUE_SP && nameStart == nameEnd) { // && buffer.position() > nameStart + 1 &&
                if (buffer.position() <= nameStart + 1) {
                    break;
                }
                nameEnd = buffer.position() - 1;
                valueStart = buffer.position();
            } else if (previousByte == HEADER_KEY_VALUE_SP && (currentByte == WHITE_SPACE || currentByte == TAB)) {
                valueStart++;
            } else if (previousByte == CR && currentByte == LF) {
                valueEnd = buffer.position() - 2;
                if (nameStart < nameEnd && nameEnd < valueStart && valueStart < valueEnd) {
                    MimeHeaderField headerField = request.headers().createHeader();
                    headerField.getName().setBytes(buffer.array(), nameStart, nameEnd - nameStart);
                    headerField.getValue().setBytes(buffer.array(), valueStart, valueEnd - valueStart);
                    return HeaderParseStatus.NEED_MORE;
                } else if (nameStart == valueEnd) {
                    return HeaderParseStatus.DONE;
                }
                break;
            }
            previousByte = currentByte;
        }
        if (currentByte == -1) {
            return HeaderParseStatus.EOF;
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid request header");
    }

    private enum HeaderParseStatus {
        DONE,
        NEED_MORE,
        EOF;
    }

    public interface HeaderDataSource {

        boolean fillHeaderBuffer() throws IOException;

        ByteBuffer getHeaderByteBuffer();
    }
}