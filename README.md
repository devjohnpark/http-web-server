# Implementation of an HTTP Web Server
---
## VERSION

- 0.0.0 
	- HTTP request data processing.
	- HTTP response data processing.
	- HTTP API processing.
- 0.0.1
	- Support virtual servers by port and domain by.
- 0.0.2
	- Support keep alive feature in HTTP/1.0 and HTTP/1.1
	- Improving exception handling
	- Non-synchronized Buffer I/O Stream for improved I/O speed (allocating a thread to process each request)
---
## How to use

#### Single web server
```java
public class WebServerLauncher {  
    public static void main(String[] args) throws IOException {  
        WebServer server = new WebServer();  
        server.getWebService().addService("/user/create", new LoginHttpApiHandler());  
        server.start();  
    }  
}
```

#### Virtual web servers 
```java
package org.dochi.webserver;  
  
import org.dochi.http.api.LoginHttpApiHandler;  
  
public class WebServerLauncher {  
    public static void main(String[] args) {  
        WebServer server1 = new WebServer(8080, "localhost");  
        server1.getWebService().addService("/user/create", new LoginHttpApiHandler());  
  
        WebServer server2 = new WebServer(7070, "example.com");  
  
        Executor.addWebServer(server1);  
        Executor.addWebServer(server2);  
        Executor.execute();  
    }  
}
```
