package org.dochi.internal.parser;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.data.HttpStatus;
import org.dochi.http.data.MimeHeaderField;
import org.dochi.internal.Request;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Http11Parser {
    private static final int SEPARATOR_SIZE = 1;
    private static final int CRLF_SIZE = 2;
    private static final int CR = '\r';
    private static final int LF = '\n';

    private final HeaderDataSource source;

    public Http11Parser(HeaderDataSource source) {
        this.source = source;
    }

    private int getByte() throws IOException {
        if (!this.source.getHeaderByteBuffer().hasRemaining() && !this.source.fillHeaderBuffer()) {
            return -1;
        }
        return this.source.getHeaderByteBuffer().get() & 0xFF;
    }

    public boolean parseRequestLine(Request request) throws IOException {
        int elementCnt = 0;
        int querySeparator = -1;
        int previousByte = -1;
        int currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int start = buffer.position();
        while ((currentByte = getByte()) != -1) {
            if (currentByte == ' ') {
                elementCnt++;
                if (elementCnt == 1) {
                    request.method().setBytes(buffer.array(), start, buffer.position() - start - SEPARATOR_SIZE);
                } else if (elementCnt == 2) { // GET /user?name=john%20park&password=1234 HTTP/1.1
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
            } else if (currentByte == '?' && querySeparator == -1) {
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
        } while (status == HeaderParseStatus.NEED_MORE); // DONE, EOF
        return status == HeaderParseStatus.DONE && request.headers().size() > 0;
    }

    private HeaderParseStatus parseHeaderField(Request request) throws IOException {
        int previousByte = -1;
        int currentByte;
        ByteBuffer buffer = source.getHeaderByteBuffer();
        int nameStart = buffer.position();
        int nameEnd = nameStart;
        int valueStart = nameStart;
        int valueEnd = nameStart;
        
        while ((currentByte = getByte()) != -1) { // 1 2
            if (currentByte == ':' && nameStart == nameEnd) { // && buffer.position() > nameStart + 1 &&
                if (buffer.position() <= nameStart + 1) {
                    break;
                }
                nameEnd = buffer.position() - 1;
                valueStart = buffer.position();
            } else if (previousByte == ':' && (currentByte == ' ' || currentByte == '\t')) {
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