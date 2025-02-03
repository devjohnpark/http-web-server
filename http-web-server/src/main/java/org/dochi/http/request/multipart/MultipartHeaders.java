package org.dochi.http.request.multipart;

import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.util.HttpParser.*;

public class MultipartHeaders {
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_TYPE = "Content-Type";

    private final Map<String, String> headers = new HashMap<>();

    public void addHeader(String line) {
        if (line == null || line.isEmpty()) {
            return;
        }
        Pair pair = parseHeaderLine(line);
        headers.put(pair.key().toLowerCase(), pair.value());
    }

    public String getHeader(String key) {
        return headers.get(key.toLowerCase());
    }

    public String getContentDisposition() {
        return getHeader(CONTENT_DISPOSITION);
    }

    public String getContentType() {
        return getHeader(CONTENT_TYPE);
    }

    public void clear() {
        if (!headers.isEmpty()) {
            headers.clear();
        }
    }
}
