package test.bbackjk.http.sample.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.sample.dto.SampleRequestDto;

import java.util.Map;

@RestClient(value = "Hello", url = "http://localhost:8080")
public interface GetClient {

    @GetMapping("/api/v1/hello2")
    String hello();

    @GetMapping("/api/v1/{value}/hello2")
    String pathVariableTest(@PathVariable String value);

    @GetMapping("/api/v1/hello3")
    String queryValueEmptyAllAnnotationTest(String key1, String key2, String key3);

    @GetMapping("/api/v1/hello3")
    String queryValueOneAnnotationTest(@RequestParam String key3);

    @GetMapping("/api/v1/hello3")
    String queryObject(@RequestParam SampleRequestDto requestDto);

    @GetMapping("/api/v1/hello3")
    String queryMap(@RequestParam Map<String, String> map);

    @GetMapping("/api/v1/{path1}/hello4")
    String queryPathHeader(
            @PathVariable("path1") String value
            , @RequestHeader("Authorization") String bearerToken
            , @RequestParam SampleRequestDto requestDto
    );
}
