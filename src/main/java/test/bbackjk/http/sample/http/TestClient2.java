package test.bbackjk.http.sample.http;

import org.springframework.web.bind.annotation.GetMapping;
import test.bbackjk.http.agent.RestTemplateAgent;
import test.bbackjk.http.annotations.RestClient;

@RestClient(url = "http://localhost:8080", agent = RestTemplateAgent.class)
public interface TestClient2 {

    @GetMapping("/api/v1/hello2")
    String hello();
}
