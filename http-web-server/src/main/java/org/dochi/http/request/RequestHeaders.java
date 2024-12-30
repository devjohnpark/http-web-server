package org.dochi.http.request;

import java.util.HashMap;
import java.util.Map;

import org.dochi.http.util.HttpParser;
/*
HTTP Request Message Header
GET / HTTP/1.1
Host: www.example.com
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3
Accept: text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate, br
Connection: keep-alive
Cookie: sessionId=abc123
Cache-Control: no-cache
Referer: https://www.google.com/
 */

/*
Not Incude Content-Length -> 411 Length Required
 */


public class RequestHeaders {
    private final Map<String, String> headers = new HashMap<>();

    public static final String HOST = "Host";
    public static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONNECTION = "Connection";
    public static final String COOKIE = "Cookie";

    public void addHeader(String line) {
        HttpParser.Pair pair = HttpParser.parseHeader(line);
        headers.put(pair.key(), pair.value());
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getCookie() { return getHeader(COOKIE); }

    public String getContentType() { return getHeader(CONTENT_TYPE); }

    public int getContentLength() {
        return parseContentLength(getHeader(CONTENT_LENGTH));
    }

    public String getConnection() { return getHeader(CONNECTION); }

    private int parseContentLength(String contentLength) {
        if (contentLength == null || contentLength.isEmpty()) {
            return 0;
        }
        int length = Integer.parseInt(contentLength);
        return Math.max(length, 0);
    }

    public void clearHeaders() {
        headers.clear();
    }
}
