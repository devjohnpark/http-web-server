package org.dochi.http.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaTypeTest {

    @Test
    void constructMediaTypeAndVerifyFields() {
        MediaType mediaType = new MediaType("text", "html", "charset", "utf-8");

        assertEquals("text", mediaType.getType());
        assertEquals("html", mediaType.getSubType());
        assertEquals("charset", mediaType.getParameterName());
        assertEquals("utf-8", mediaType.getParameterValue());
        assertEquals("utf-8", mediaType.getCharset());
    }

    @Test
    void getTypeSubTypeReturnsCorrectFormat() {
        MediaType mediaType = new MediaType("application", "json", null, null);
        assertEquals("application/json", mediaType.getTypeSubType());
    }

    @Test
    void toStringIncludesParameterWhenPresent() {
        MediaType mediaType = new MediaType("text", "plain", "charset", "utf-8");
        assertEquals("text/plain; charset=utf-8", mediaType.toString());
    }

    @Test
    void toStringOmitsParameterWhenAbsent() {
        MediaType mediaType = new MediaType("image", "png", null, null);
        assertEquals("image/png", mediaType.toString());
    }

    @Test
    void parseMediaTypeParsesValidTypeWithParameter() {
        MediaType mediaType = MediaType.parseMediaType("text/html; charset=utf-8");

        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("html", mediaType.getSubType());
        assertEquals("charset", mediaType.getParameterName());
        assertEquals("utf-8", mediaType.getParameterValue());
        assertEquals("utf-8", mediaType.getCharset());
    }

    @Test
    void parseMediaTypeParsesValidTypeWithoutParameter() {
        MediaType mediaType = MediaType.parseMediaType("application/json");

        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("json", mediaType.getSubType());
        assertNull(mediaType.getParameterName());
        assertNull(mediaType.getParameterValue());
        assertNull(mediaType.getCharset());
    }

    @Test
    void parseMediaTypeReturnsNullWhenInputIsNull() {
        assertNull(MediaType.parseMediaType(null));
    }

    @Test
    void parseMediaTypeReturnsNullWhenInputIsBlank() {
        assertNull(MediaType.parseMediaType("   "));
    }

    @Test
    void parseMediaTypeReturnsNullWhenMissingSubtype() {
        assertNull(MediaType.parseMediaType("text"));
    }

    @Test
    void parseMediaTypeThrowsExceptionOnInvalidParameterFormat() {
        assertThrows(IllegalStateException.class, () -> MediaType.parseMediaType("text/html; charset"));
    }

    @Test
    void getCharsetReturnsNullWhenParameterIsNotCharset() {
        MediaType mediaType = new MediaType("text", "plain", "encoding", "utf-8");
        assertNull(mediaType.getCharset());
    }

    @Test
    void getCharsetReturnsNullWhenCharsetValueIsEmpty() {
        MediaType mediaType = new MediaType("text", "plain", "charset", "");
        assertNull(mediaType.getCharset());
    }
}
