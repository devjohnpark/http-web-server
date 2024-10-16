package util;

import java.util.HashMap;
import java.util.Map;

// Request Line: GET /user/create?userId=john&password=1234&name=john park HTTP/1.1
// url: /user/create?userId=john&password=1234&name=john park
// path: /user/create
// query string: userId=john&password=1234&name=john park
// ?: resource와 query string 간의 분리
// &: query parameter 간의 분리
// =: key, value 간의 분리
public class HttpRequestParser {
    public static Map<String, String> parseQueryString(String queryString) {
        return parseValues(queryString, "&");
    }

    private static Map<String, String> parseValues(String values, String separator) {
        if (values == null || values.isEmpty()) {
            return new HashMap<>();
        }
        return parseParameters(values.split(separator));
    }

    private static Map<String, String> parseParameters(String[] parameters) {
        Map<String, String> map = new HashMap<>();
        for (String parameter : parameters) {
            Pair pair = getKeyValue(parameter, "="); // not null
            if (pair != null) {
                map.put(pair.key, pair.value);
            }
        }
        return map;
    }

    public static Pair parseUrl(String url) {
        Pair pair = getKeyValue(url, "\\?");
        return setEmptyValue(url, pair);
    }

    // key: can't be empty, value: can be empty
    private static Pair getKeyValue(String keyValue, String separator) {
        if (keyValue == null || keyValue.isEmpty()) {
            return null;
        }
        String[] pair = keyValue.split(separator);
        if (pair.length > 2 || pair[0].isEmpty()) {
            return null;
        }
        if (pair.length > 1) {
            return new Pair(pair[0].trim(), pair[1].trim());
        }
        return new Pair(pair[0].trim(), "");
    }

    public static Pair parseHeader(String header) {
        Pair pair = getKeyValue(header, ": ");
        return setEmptyValue(header, pair);
    }

    private static Pair setEmptyValue(String header, Pair pair) {
        if (pair != null) {
            return new Pair(pair.key, pair.value);
        }
        return new Pair(header, "");
    }

    public static String[] parseRequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3) {
            throw new IllegalStateException();
        }
        return tokens;
    }

    public record Pair(String key, String value) {}
}

