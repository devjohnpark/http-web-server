package org.dochi.http.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestParametersTest {

    private RequestParameters parameters;

    @BeforeEach
    void setUp() {
        parameters = new RequestParameters();
    }

    @Test
    void addRequestParameters_requestLine() {
        String queryStringWithUrlEncoded = "name=john%20park&age=20";
        parameters.addRequestParameters(queryStringWithUrlEncoded);
        assertThat(parameters.getParameter("name")).isEqualTo("john park");
        assertThat(parameters.getParameter("age")).isEqualTo("20");
    }

    @Test
    void addRequestParameters_formUrlEncoded() {
        String queryStringWithUrlEncoded = "name=john+park&age=20";
        parameters.addRequestParameters(queryStringWithUrlEncoded);
        assertThat(parameters.getParameter("name")).isEqualTo("john park");
        assertThat(parameters.getParameter("age")).isEqualTo("20");
    }


    @Test
    void addRequestParameters_null() {
        parameters.addRequestParameters(null);
        assertThat(parameters.getParameter("name")).isNull();
        assertThat(parameters.getParameter("age")).isNull();
    }

    @Test
    void addRequestParameters_empty() {
        parameters.addRequestParameters("");
        assertThat(parameters.getParameter("")).isNull();
        assertThat(parameters.getParameter("")).isNull();
    }
}