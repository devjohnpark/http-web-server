package org.dochi.internal;

import org.dochi.connector.Connector;

// reference web service container
public class Adapter {
    private final Connector connector;

    public Adapter(Connector connector) {
        this.connector = connector;
    }

    // connector.Request/Response 생성

}
