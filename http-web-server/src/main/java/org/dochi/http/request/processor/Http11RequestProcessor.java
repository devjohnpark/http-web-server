package org.dochi.http.request.processor;

import org.dochi.http.exception.HttpStatusException;

import org.dochi.http.monitor.ContentLengthValidator;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.request.multipart.Part;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.request.stream.HttpBufferedInputStream;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// Http11RequestProcessor로 변경
public class Http11RequestProcessor extends AbstractHttpRequestProcessor {
    private static final Logger log = LoggerFactory.getLogger(Http11RequestProcessor.class);
    private final ContentLengthValidator contentLengthValidator;
    private final Http11RequestStream requestStream;

    public Http11RequestProcessor(Http11RequestStream requestStream, HttpReqConfig httpReqConfig) {
        super(httpReqConfig);
        this.requestStream = requestStream;
        this.contentLengthValidator = new ContentLengthValidator(sizeMonitor.getContentMonitor());
    }

    @Override
    public boolean isProcessHeader(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        if (isProcessRequestLine(sizeMonitor)) {
            processHeaders(sizeMonitor);
            return true;
        }
        return false;
    }

    private boolean isProcessRequestLine(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String requestLine = requestStream.readLineString(sizeMonitor);
        if (isEOFOnCloseWait(requestLine)) {
            return false;
        }
        try {
            request.getMetadata().addRequestLine(requestLine);
            return true;
        } catch (IllegalArgumentException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private boolean isEOFOnCloseWait(String requestLine) {
        return requestLine == null;
    }

    private void processHeaders(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String line;
        while ((line = requestStream.readLineString(sizeMonitor)) != null) {
            if (line.isEmpty()) {
                return;
            }
            request.getHeaders().addHeaderLine(line);
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected end of stream while reading HTTP header");
    }

    @Override
    public byte[] getAllBody() throws IOException, HttpStatusException {
        return contentLengthValidator.validateContentOnRead(request.getHeaders().getContentLength(), contentLength ->
            requestStream.readAllBody(contentLength, sizeMonitor.getBodyMonitor())
        );
    }

    // 지연 읽기가 아직 커널 메모리에 저장되기 때문에 애플리케이션 메모리 과부하를 줄읽수 있다. -> parts가 비었으면 처음으로 파싱을 수행
    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        ResourceType resourceType = ResourceType.MULTIPART;
        String contentType = request.getHeaders().getContentType();
        String boundary = resourceType.getMediaTypeParamValue(contentType);
        if (shouldProcessMultipart(resourceType, contentType)) {
            contentLengthValidator.validateContentOnRead(request.getHeaders().getContentLength(), contentLength -> {
                multipartProcessor.processParts(
                        requestStream,
                        boundary,
                        request
                    );
                    return null;
                }
            );
        }
        return request.getMultipart().getPart(partName);
    }

    private boolean shouldProcessMultipart(ResourceType resourceType, String contentType) {
        return !request.getMultipart().isLoad() && resourceType.isEqualMimeType(contentType);
    }

    @Override
    public HttpBufferedInputStream getInputStream() { return requestStream.getInputStream(); }
}
