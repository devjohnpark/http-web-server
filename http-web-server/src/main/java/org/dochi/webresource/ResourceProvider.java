package org.dochi.webresource;

import java.io.IOException;
import java.nio.file.Path;

public interface ResourceProvider {
    Resource getResource(String resourcePath);
//    Resource getResource(Path requestPath);
//    Resource getResource(String fileName);
}
