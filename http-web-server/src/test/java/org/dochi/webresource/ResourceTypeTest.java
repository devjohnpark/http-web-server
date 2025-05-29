package org.dochi.webresource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTypeTest {

    @Test
    void getExtension_ReturnsCorrectExtension() {
        assertEquals(".txt", ResourceType.TEXT.getExtension());
        assertEquals(".html", ResourceType.HTML.getExtension());
        assertEquals(".js", ResourceType.JS.getExtension());
        assertEquals(".xml", ResourceType.XML.getExtension());
        assertEquals(".css", ResourceType.CSS.getExtension());
        assertEquals(".ico", ResourceType.ICO.getExtension());
        assertEquals(".png", ResourceType.PNG.getExtension());
        assertEquals(".jpeg", ResourceType.JPEG.getExtension());
        assertEquals(".jpg", ResourceType.JPG.getExtension());
        assertEquals(".json", ResourceType.JSON.getExtension());
        assertNull(ResourceType.URL.getExtension());
        assertNull(ResourceType.MULTIPART.getExtension());
        assertNull(ResourceType.OCTET_STREAM.getExtension());
        assertNull(ResourceType.UNKNOWN.getExtension());
    }

    @Test
    void getMimeType_ReturnsCorrectMimeType() {
        assertEquals("text/plain", ResourceType.TEXT.getMimeType());
        assertEquals("text/html", ResourceType.HTML.getMimeType());
        assertEquals("application/javascript", ResourceType.JS.getMimeType());
        assertEquals("application/xml", ResourceType.XML.getMimeType());
        assertEquals("text/css", ResourceType.CSS.getMimeType());
        assertEquals("image/x-icon", ResourceType.ICO.getMimeType());
        assertEquals("image/png", ResourceType.PNG.getMimeType());
        assertEquals("image/jpeg", ResourceType.JPEG.getMimeType());
        assertEquals("image/jpg", ResourceType.JPG.getMimeType());
        assertEquals("application/json", ResourceType.JSON.getMimeType());
        assertEquals("application/x-www-form-urlencoded", ResourceType.URL.getMimeType());
        assertEquals("multipart/form-data", ResourceType.MULTIPART.getMimeType());
        assertEquals("application/octet-stream", ResourceType.OCTET_STREAM.getMimeType());
        assertNull(ResourceType.UNKNOWN.getMimeType());
    }

    @Test
    void getContentType_WithParameter_TextBasedTypes() {
        assertEquals("text/plain; charset=utf-8", ResourceType.TEXT.getContentType("utf-8"));
        assertEquals("text/html; charset=utf-8", ResourceType.HTML.getContentType("utf-8"));
        assertEquals("application/javascript; charset=utf-8", ResourceType.JS.getContentType("utf-8"));
        assertEquals("application/xml; charset=utf-8", ResourceType.XML.getContentType("utf-8"));
        assertEquals("text/css; charset=utf-8", ResourceType.CSS.getContentType("utf-8"));
    }

    @Test
    void getContentType_WithParameter_MultipartType() {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        assertEquals("multipart/form-data; boundary=" + boundary, 
                     ResourceType.MULTIPART.getContentType(boundary));
    }

    @Test
    void getContentType_WithParameter_NonTextTypes() {
        assertEquals("image/png", ResourceType.PNG.getContentType("utf-8"));
        assertEquals("image/jpeg", ResourceType.JPEG.getContentType("utf-8"));
        assertEquals("image/jpg", ResourceType.JPG.getContentType("utf-8"));
        assertEquals("image/x-icon", ResourceType.ICO.getContentType("utf-8"));
        assertEquals("application/json", ResourceType.JSON.getContentType("utf-8"));
    }

    @Test
    void getContentType_WithNullParameter() {
        assertEquals("text/plain", ResourceType.TEXT.getContentType(null));
        assertEquals("text/html", ResourceType.HTML.getContentType(null));
        assertEquals("multipart/form-data", ResourceType.MULTIPART.getContentType(null));
    }

    @Test
    void getContentType_WithEmptyParameter() {
        assertEquals("text/plain", ResourceType.TEXT.getContentType(""));
        assertEquals("text/html", ResourceType.HTML.getContentType(""));
        assertEquals("multipart/form-data", ResourceType.MULTIPART.getContentType(""));
    }

    @Test
    void getContentTypeParamValue_ValidContentTypes() {
        assertEquals("utf-8", ResourceType.TEXT.getContentTypeParamValue("text/plain; charset=utf-8"));
        assertEquals("boundary123", ResourceType.MULTIPART.getContentTypeParamValue("multipart/form-data; boundary=boundary123"));
        assertEquals("iso-8859-1", ResourceType.HTML.getContentTypeParamValue("text/html; charset=iso-8859-1"));
    }

    @Test
    void getContentTypeParamValue_InvalidContentTypes() {
        assertNull(ResourceType.TEXT.getContentTypeParamValue(null));
        assertNull(ResourceType.TEXT.getContentTypeParamValue(""));
        assertNull(ResourceType.TEXT.getContentTypeParamValue("text/plain"));
        assertNull(ResourceType.TEXT.getContentTypeParamValue("text/plain; charset"));
        assertNull(ResourceType.TEXT.getContentTypeParamValue("text/plain; charset="));
    }

    @Test
    void fromFilePath_NullPath() {
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromFilePath(null));
    }

    @Test
    void fromMimeType_NullMimeType() {
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromMimeType(null));
    }

    @Test
    void fromMimeType_EmptyMimeType() {
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromMimeType(""));
    }

    @Test
    void getContentTypeParamValue_WithSpaces() {
        assertEquals("utf-8", ResourceType.TEXT.getContentTypeParamValue("text/plain; charset=utf-8"));
        assertEquals("utf-8", ResourceType.TEXT.getContentTypeParamValue("text/plain;charset=utf-8"));
        assertEquals("utf-8", ResourceType.TEXT.getContentTypeParamValue("text/plain;  charset=utf-8"));
    }

    @Test
    void edgeCases_FileNames() {
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromFilePath(Paths.get("")));
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromFilePath(Paths.get(".")));
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromFilePath(Paths.get("..")));
        assertEquals(ResourceType.HTML, ResourceType.fromFilePath(Paths.get("a.b.c.html")));
        assertEquals(ResourceType.TEXT, ResourceType.fromFilePath(Paths.get("file_name.txt")));
    }
}