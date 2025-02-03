package org.dochi.http.api;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.RequestHeadersGetter;
import org.dochi.http.request.data.RequestMetadataGetter;
import org.dochi.http.request.data.RequestParametersGetter;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;

public interface HttpApiRequest extends RequestMetadataGetter, RequestHeadersGetter, RequestParametersGetter {
    byte[] getAllBody() throws IOException;
    String getAllBodyAsString() throws IOException;
    Part getPart(String partName) throws IOException, HttpStatusException;
}
