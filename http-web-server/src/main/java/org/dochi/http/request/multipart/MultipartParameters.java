package org.dochi.http.request.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.util.HttpParser.parseContentDisposition;

public class MultipartParameters {
    private static final Logger log = LoggerFactory.getLogger(MultiPartProcessor.class);
    private static final String NAME_PARAMETER_KEY = "name";
    private static final String FILENAME_PARAMETER_KEY = "filename";
    private final Map<String, String> parameters = new HashMap<String, String>();

    public void addContentDispositionParameters(String contentDispositionHeaderValue) {
        if (contentDispositionHeaderValue == null || contentDispositionHeaderValue.isEmpty()) { return; }
        parameters.putAll(parseContentDisposition(contentDispositionHeaderValue));
    }

    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value != null) {
            return value.replace("\"", "");
        }
        return null;
    }

    public String getNameParamValue() { return getParameter(NAME_PARAMETER_KEY); }

    public String getFileNameParamValue() { return getParameter(FILENAME_PARAMETER_KEY); }

    public void clear() {
        if (!parameters.isEmpty()) {
            parameters.clear();
        }
    }
}
