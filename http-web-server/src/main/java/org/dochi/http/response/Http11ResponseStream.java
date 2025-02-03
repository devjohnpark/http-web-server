package org.dochi.http.response;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Http11ResponseStream extends BufferedOutputStream {
    public Http11ResponseStream(OutputStream out) {
        super(out);
    }
}
