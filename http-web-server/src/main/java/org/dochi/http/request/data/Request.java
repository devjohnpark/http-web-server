package org.dochi.http.request.data;

import org.dochi.http.request.multipart.Multipart;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Request implements Cloneable {
    private final RequestMetadata metadata = new RequestMetadata();
    private final RequestHeaders headers = new RequestHeaders();
    private final RequestParameters parameters = new RequestParameters();
    private final Multipart multipart = new Multipart();

    public RequestMetadata getMetadata() {
        return metadata;
    }

    public RequestHeaders getHeaders() {
        return headers;
    }

    public RequestParameters getParameters() {
        return parameters;
    }

    public Multipart getMultipart() {
        return multipart;
    }

    public void clear() throws IOException {
        headers.clear();
        parameters.clear();
        multipart.clear();
    }

    @Override
    public Request clone() throws CloneNotSupportedException {
        return (Request) super.clone();
    }
}
