package org.dochi.http.request.processor;

import org.dochi.http.exception.HttpStatusException;

import org.dochi.http.monitor.ContentLengthValidator;
import org.dochi.http.monitor.MessageSizeMonitor;
import org.dochi.http.request.multipart.Part;
import org.dochi.http.request.stream.Http11RequestStream;
import org.dochi.http.request.stream.SocketBufferedInputStream;
import org.dochi.http.response.HttpStatus;
import org.dochi.webresource.ResourceType;
import org.dochi.webserver.config.HttpReqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        return processRequestLine(sizeMonitor) && processHeaders(sizeMonitor);
    }

    private boolean processRequestLine(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String requestLine = requestStream.readHeader(sizeMonitor);
        if (isEOFOnCloseWait(requestLine)) {
            return false;
        }
        try {
            request.metadata().addRequestLine(requestLine);
            return true;
        } catch (IllegalArgumentException e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private boolean isEOFOnCloseWait(String requestLine) {
        return requestLine == null;
    }

    private boolean processHeaders(MessageSizeMonitor sizeMonitor) throws IOException, HttpStatusException {
        String line;
        while ((line = requestStream.readHeader(sizeMonitor)) != null) {
            if (line.isEmpty()) {
                return true;
            }
            request.headers().addHeaderLine(line);
        }
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unexpected end of stream while reading HTTP header");
    }

    @Override
    public byte[] getAllBody() throws IOException, HttpStatusException {
        return contentLengthValidator.validateContentOnRead(request.headers().getContentLength(), contentLength ->
            requestStream.readAllBody(contentLength, sizeMonitor.getBodyMonitor())
        );
    }

    // 지연 읽기가 아직 커널 메모리에 저장되기 때문에 애플리케이션 메모리 과부하를 줄읽수 있다. -> parts가 비었으면 처음으로 파싱을 수행
    @Override
    public Part getPart(String partName) throws IOException, HttpStatusException {
        ResourceType resourceType = ResourceType.MULTIPART;
        String contentType = request.headers().getContentType();
        String boundary = resourceType.getContentTypeParamValue(contentType);
        if (shouldProcessMultipart(resourceType, contentType)) {
            contentLengthValidator.validateContentOnRead(request.headers().getContentLength(), contentLength -> {
                multipartProcessor.processParts(
                        requestStream,
                        boundary,
                        request
                    );
                    return null;
                }
            );
        }
        return request.multipart().getPart(partName);
    }

    private boolean shouldProcessMultipart(ResourceType resourceType, String contentType) {
        return !request.multipart().isLoad() && resourceType.isEqualMimeType(contentType);
    }

    @Override
    public SocketBufferedInputStream getInputStream() { return requestStream.getInputStream(); }
}
