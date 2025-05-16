package org.dochi.inputbuffer.internal;

import org.dochi.inputbuffer.connector.Connector;
import org.dochi.http.response.processor.HttpResponseProcessor;

// reference web service container
public class InternalAdapter implements Adapter {
    private final Connector connector;

    public InternalAdapter(Connector connector) {
        this.connector = connector;
    }

    //

    @Override
    public void service(Request req, HttpResponseProcessor res) throws Exception {
        org.dochi.inputbuffer.connector.Request request = this.connector.createRequest();
        request.setInternalRequest(req);


    }

    // connector.Request/Response 생성

}
