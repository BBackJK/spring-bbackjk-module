package test.bbackjk.http.sample.http;

import org.springframework.web.bind.annotation.GetMapping;
import test.bbackjk.http.annotations.RestClient;

@RestClient(url = "http://localhost:8080")
public interface TestClient {

    @GetMapping("/api/v1/hello2")
    String hello();
}
