//package org.dochi.http.request.data;
//
//import org.dochi.http.util.HttpParser;
//import org.dochi.http.util.HttpParser.Pair;
//
//public class RequestLine {
//    private final HttpMethod method;
//    private final String path;
//    private final String queryString;
//    private final HttpVersion version;
//
//    private RequestLine(HttpMethod method, String path, String queryString, HttpVersion version) {
//        this.method = method;
//        this.path = path;
//        this.queryString = queryString;
//        this.version = version;
//    }
//
//    public static RequestLine createFromRequestLine(String requestLine) {
//        String[] tokens = HttpParser.parseRequestLine(requestLine);
//        HttpMethod method = HttpMethod.valueOf(tokens[0]);
//        Uri uri = getUri(tokens[1]);
//        HttpVersion version = HttpVersion.fromString(tokens[2]);
//        return new RequestLine(method, uri.path(), uri.queryString(), version);
//    }
//
//    private static Uri getUri(String uri) {
//        Pair pair = HttpParser.parseRequestUri(uri);
//        String path = pair.key();
//        String queryString = pair.value();
//        return new Uri(path, queryString);
//    }
//
//    private record Uri(String path, String queryString) {}
//
//    public HttpMethod getMethod() { return method; }
//
//    public String getPath() { return path; }
//
//    public String getQueryString() { return queryString; }
//
//    public HttpVersion getVersion() { return version; }
//}
