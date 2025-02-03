//package org.dochi.webresource;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//class WebResourceProviderTest {
//    WebResourceProvider webResourceProvider = new WebResourceProvider(Path.of("webapp"));
//
//    @BeforeEach
//    void setUp() {
//        webResourceProvider = new WebResourceProvider(Path.of("webapp"));
//    }
//
//    @Test
//    void getWebResource() {
//        assertThrows(IllegalArgumentException.class, () -> new WebResourceProvider(Path.of("/webapp")));
//    }
//
//    @Test
//    void getResource_() {
////        Resource resource = webResourceProvider.getResource("/");
////        assertThat(resource.isEmpty()).isEqualTo(false);
//        assertThrows(IllegalArgumentException.class, () -> webResourceProvider.getResource("/"));
//    }
//
//    @Test
//    void getResource_html_file() {
//        Resource resource = webResourceProvider.getResource("index.html");
//        assertThat(resource.isEmpty()).isEqualTo(false);
//    }
//
//    @Test
//    void getResource_non_exist_file() {
//        Resource resource = webResourceProvider.getResource("hi.html");
//        assertThat(resource.isEmpty()).isEqualTo(true);
//    }
//
//    @Test
//    void getResource_root_path() {
//        Resource resource = webResourceProvider.getResource(Paths.get("/"));
//        assertThat(resource.isEmpty()).isEqualTo(false);
//    }
//
//    @Test
//    void getResource_index_path() {
//        Resource resource = webResourceProvider.getResource(Paths.get("/index.html"));
//        assertThat(resource.isEmpty()).isEqualTo(false);
//    }
//
//    @Test
//    void getResource_non_exist_path() {
//        Resource resource = webResourceProvider.getResource(Paths.get("/index"));
//        assertThat(resource.isEmpty()).isEqualTo(true);
//    }
//}