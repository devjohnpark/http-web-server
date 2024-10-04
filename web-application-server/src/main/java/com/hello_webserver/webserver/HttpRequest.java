package com.hello_webserver.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

// 클라이언트 요청 읽기(HttpRequest)
// 1. HTTP 요청 메세지의 헤더를 읽는다.
//      1. 헤더에서 HTTP Method가 유효한지 확인
//      2. 헤더에서 Resource의 경로가 유효한지 확인
// 2. HTTP 요청 메세지의 바디를 읽는다.
public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    public RequestLine readRequestHeader(BufferedReader br) {
        try {
            String line = br.readLine();
            String[] tokens = line.split(" ");
            if (tokens.length >= 2) {
                return vaildRequestLine(tokens[0], tokens[1]);
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    private boolean isMethodAllowed(String method) {
        return method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE");
    }

    // check endpoint
    private boolean isResourcePathAllowed(String path) {
        int lastSlashIndex = path.lastIndexOf("/");
        String filePath = HttpRequestUtils.ROOT_PATH + path.substring(0, lastSlashIndex);
        return Files.exists(Path.of(filePath));
    }

    private RequestLine vaildRequestLine(String method, String path) {
        if (isMethodAllowed(method) && isResourcePathAllowed(path)) {
            return new RequestLine(method, path);
        }
        return null;
    }
}
