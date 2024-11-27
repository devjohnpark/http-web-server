package org.dochi.webresource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebResourceProviderTest {
    private final WebResourceProvider webResourceProvider = new WebResourceProvider("webapp");

    @Test
    void getResource_root_path() {
        Resource resource = webResourceProvider.getResource("/");
        assertThat(resource.isEmpty()).isEqualTo(false);
    }

    @Test
    void getResource_index_path() {
        Resource resource = webResourceProvider.getResource("/index.html");
        assertThat(resource.isEmpty()).isEqualTo(false);
    }

    @Test
    void getResource_non_exist_path() {
        Resource resource = webResourceProvider.getResource("/index");
        assertThat(resource.isEmpty()).isEqualTo(true);
    }
}