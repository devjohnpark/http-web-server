package org.dochi.http.response;

public enum HttpStatus {
    OK(200, "OK", ""),
    BAD_REQUEST(400, "Bad Request", "The server cannot or will not process the request."),
    NOT_FOUND(404, "Not Found", "The requested resource could not be found."),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", "The request method is known by the server but is not supported by the target resource."),
    NOT_IMPLEMENTED(501, "Not Implemented", "The request method is not supported by the server and cannot be handled.");

    private final int code;
    private final String codeMessage;
    private final String description;

    HttpStatus(int code, String codeMessage, String description) {
        this.code = code;
        this.codeMessage = codeMessage;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return codeMessage;
    }

    public String getDescription() {
        return description;
    }
}
