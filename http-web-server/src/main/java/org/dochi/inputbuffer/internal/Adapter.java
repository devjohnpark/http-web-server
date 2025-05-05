package org.dochi.inputbuffer.internal;


import org.dochi.http.response.processor.HttpResponseProcessor;

public interface Adapter {
    void service(Request req, HttpResponseProcessor res) throws Exception;
}
