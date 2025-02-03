package org.dochi.http.request.processor;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.api.HttpApiRequest;
import org.dochi.http.request.data.Request;
import org.dochi.http.request.stream.HttpBufferedInputStream;

import java.io.IOException;

//// RequestMetadataGetter, RequestHeadersGetter, RequestParametersGetter: HttpApiHandler에서만 호출 필요
//// HttpRequestProcessor 구현체에서는 RequestMetadata, RequestHeaders, RequestParameters 참조하고 있어서 필요없음
//// HttpRequestProcessor 구현체에서 RequestMetadata, RequestHeaders, RequestParameters 객체를 HttpApiRequest 구현체에 넘길수도 있음
//public interface HttpRequestProcessor extends RequestMetadataGetter, RequestHeadersGetter, RequestParametersGetter {
//    // 개발자만 호출 필요
//    String getHeader(String key);
//    byte[] getAllBody() throws IOException, HttpStatusException;
//    String getAllBodyAsString() throws HttpStatusException, IOException;
//    Part getPart(String partName) throws IOException, HttpStatusException;
//
//    // 매우중요: HttpProcessor에서 제어하므로 해당 객체에서만 호출 필요
//    boolean isPrepareRequest() throws IOException, HttpStatusException;
//    void refresh() throws IOException;
//
//    // RequestMetadata getRequestMetadata();
//    // RequestHeaders getHeaders();
//}
//

// RequestMetadataGetter, RequestHeadersGetter, RequestParametersGetter: HttpApiHandler에서만 호출 필요
// HttpRequestProcessor 구현체에서는 RequestMetadata, RequestHeaders, RequestParameters 참조하고 있어서 필요없음
// HttpRequestProcessor 구현체에서 RequestMetadata, RequestHeaders, RequestParameters 객체를 HttpApiRequest 구현체에 넘길수도 있음
public interface HttpRequestProcessor extends HttpApiRequest {
    // 매우중요: HttpProcessor에서 HttpRequestProcessor을 제어하므로 해당 객체에서만 호출 필요
    boolean isPrepareHeader() throws IOException, HttpStatusException;
    void refresh() throws IOException;
    HttpBufferedInputStream getInputStream();
    Request getParsedRequest();
}

