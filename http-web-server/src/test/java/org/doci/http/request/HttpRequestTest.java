package org.doci.http.request;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {
    private HttpRequest httpRequest;
    private String fileName;

    private void createHttpRequest(String fileName) {
        try {
            String testDir = "./src/test/resources/";
            InputStream in = new FileInputStream(new File(testDir + fileName));
            httpRequest = new HttpRequest(in);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_only_path() {
        // given, when
        createHttpRequest("http_req_get_only_path.txt");

        // then
        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.getPath()).isEqualTo("/");
        assertThat(httpRequest.getRequestParameter("name")).isNull();
        assertThat(httpRequest.getRequestParameter("")).isNull();
        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(httpRequest.getHeader(RequestHeaders.CONNECTION)).isEqualTo(httpRequest.getConnection());
    }

    @Test
    void get_path_queryString() {
        // given, when
        createHttpRequest("http_req_get_query-string.txt");

        // then
        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(httpRequest.getPath()).isEqualTo("/user");
        assertThat(httpRequest.getRequestParameter("name")).isEqualTo("john park");
        assertThat(httpRequest.getRequestParameter("password")).isEqualTo("1234");
        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(httpRequest.getHeader(RequestHeaders.CONNECTION)).isEqualTo(httpRequest.getConnection());
    }

    @Test
    void post() {
        // given, when
        createHttpRequest("http_req_post.txt");

        // then
        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getRequestParameter("userId")).isEqualTo("john park");
        assertThat(httpRequest.getRequestParameter("password")).isEqualTo("1234");
        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(httpRequest.getHeader(RequestHeaders.CONNECTION)).isEqualTo(httpRequest.getConnection());
    }

    @Test
    void post_non_contentLength() {
        // given, when
        createHttpRequest("http_req_post_non_content-length.txt");

        // then
        assertThat(httpRequest.getRequestParameter("userId")).isNull();
        assertThat(httpRequest.getRequestParameter("password")).isNull();
    }

    @Test
    void post_negative_contentLength() {
        // given, when
        createHttpRequest("http_req_post_negative_content-length.txt");

        // then
        assertEquals(0, httpRequest.getContentLength());
        assertThat(httpRequest.getRequestParameter("userId")).isNull();
        assertThat(httpRequest.getRequestParameter("password")).isNull();
    }

    @Test
    void post_non_contentType() {
        // given, when
        createHttpRequest("http_req_post_non_content-type.txt");

        // then
        assertThat(httpRequest.getRequestParameter("userId")).isNull();
        assertThat(httpRequest.getRequestParameter("password")).isNull();
    }

    @Test
    void post_requestLine_body_params_duplication() {
        // given, when
        createHttpRequest("http_req_post_request-params_duplication.txt");

        // then
        assertThat(httpRequest.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getRequestParameter("userId")).isEqualTo("john park");
        assertThat(httpRequest.getRequestParameter("password")).isEqualTo("1234");
        assertThat(httpRequest.getRequestParameter("num")).isEqualTo("123445");
        assertThat(httpRequest.getHttpVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(httpRequest.getHeader(RequestHeaders.CONNECTION)).isEqualTo(httpRequest.getConnection());
    }

    @Test
    void post_getAllBody() throws IOException {
        // given, when
        createHttpRequest("http_req_post_content-type_text.txt");

        // then
        assertThat(httpRequest.getAllBody()).isEqualTo("hello world");
    }
}