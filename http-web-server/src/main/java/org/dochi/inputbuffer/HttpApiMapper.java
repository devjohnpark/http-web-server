//package org.dochi.buffer;
//
//import org.dochi.connector.Connector;
//import org.dochi.http.api.HttpApiHandler;
//import org.dochi.webserver.attribute.WebService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//// Map<Path, HttpApiHandler> 타입을 자꾸 수정해야됨 -> 삭제 필요
//public class HttpApiMapper {
//    private static final Logger log = LoggerFactory.getLogger(HttpApiMapper.class);
//    private final Connector connector = new Connector();
//    private final WebService webService;
//
//    public HttpApiMapper(WebService webService) {
//        this.webService = webService;
//    }
//
//    public HttpApiHandler getHttpApiHandler(String path) {
//        HttpApiHandler httpApiHandler = webService.getServices().get(path);
//        if (httpApiHandler == null) {
//            return webService.getServices().get("/");
//        }
//        return httpApiHandler;
//
////        HttpApiHandler httpApiHandler = requestMappings.get(path);
////        if (httpApiHandler == null) {
////            return requestMappings.get(rootPath);
////        }
////        return httpApiHandler;
//    }
//}
