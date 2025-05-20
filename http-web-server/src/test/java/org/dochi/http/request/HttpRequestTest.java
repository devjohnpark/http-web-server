//package org.dochi.http.request;
//
//import org.junit.jupiter.api.Test;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class HttpRequestTest {
//    private Request httpRequestHandler;
//
//    private void createHttpRequest(String fileName) throws IOException {
//        String testDir = "./src/test/resources/";
//        BufferedSocketInputStream in = new FileInputStream(new File(testDir + fileName));
//        httpRequestHandler = new Request(in);
//        httpRequestHandler.isPrepareRequest();
//    }
//
//    @Test
//    void invalid_requestLine() throws IOException {
//        assertThrows(IllegalArgumentException.class, () -> createHttpRequest("http_req_invalid_request_line1.txt"));
//        assertThrows(IllegalArgumentException.class, () -> createHttpRequest("http_req_invalid_request_line2.txt"));
//    }
//
//    @Test
//    void get_path_queryString() throws IOException {
//        // given, when
////        createHttpRequest("http_req_get_query-string.txt");
//        String message = """
//                GET /user?name=john%20park&password=1234 HTTP/1.1\r
//                Connection: keep-alive\r
//                \r
//                """;
//
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
//        Request request = new Request(byteArrayInputStream);
//        request.isPrepareRequest();
//
//
//        // then
//        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
//        assertThat(request.getPath()).isEqualTo("/user");
//        assertThat(request.getRequestParameter("name")).isEqualTo("john park");
//        assertThat(request.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(request.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(request.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post.txt");
//
//        // then
//        assertThat(httpRequestHandler.getMethod()).isEqualTo(HttpMethod.POST);
//        assertThat(httpRequestHandler.getPath()).isEqualTo("/user/create");
//        assertThat(httpRequestHandler.getRequestParameter("userId")).isEqualTo("john park");
//        assertThat(httpRequestHandler.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(httpRequestHandler.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(httpRequestHandler.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post_non_contentLength() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_non_content-length.txt");
//
//        // then
//        assertThat(httpRequestHandler.getRequestParameter("userId")).isNull();
//        assertThat(httpRequestHandler.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_negative_contentLength() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_negative_content-length.txt");
//
//        // then
//        assertEquals(0, httpRequestHandler.getContentLength());
//        assertThat(httpRequestHandler.getRequestParameter("userId")).isNull();
//        assertThat(httpRequestHandler.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_non_contentType() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_non_content-type.txt");
//
//        // then
//        assertThat(httpRequestHandler.getRequestParameter("userId")).isNull();
//        assertThat(httpRequestHandler.getRequestParameter("password")).isNull();
//    }
//
//    @Test
//    void post_requestLine_body_params_duplication() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_request-params_duplication.txt");
//
//        // then
//        assertThat(httpRequestHandler.getMethod()).isEqualTo(HttpMethod.POST);
//        assertThat(httpRequestHandler.getPath()).isEqualTo("/user/create");
//        assertThat(httpRequestHandler.getRequestParameter("userId")).isEqualTo("john park");
//        assertThat(httpRequestHandler.getRequestParameter("password")).isEqualTo("1234");
//        assertThat(httpRequestHandler.getRequestParameter("num")).isEqualTo("123445");
//        assertThat(httpRequestHandler.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
//        assertThat(httpRequestHandler.getConnection()).isEqualTo("keep-alive");
//    }
//
//    @Test
//    void post_getBodyAsString() throws IOException {
//        // given, when
//        createHttpRequest("http_req_post_content-type_text.txt");
//
//        // then
//        assertThat(httpRequestHandler.getBodyAsString()).isEqualTo("hello world");
//    }
//}