package test.bbackjk.http.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestResponseController {

    @GetMapping("/api/v1/hello2")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/api/v1/{value}/hello2")
    String pathVariableTest(@PathVariable String value) {
        return value;
    }

    @GetMapping("/api/v1/hello3")
    String query1(String key1, String key2, String key3) {
        log.info("key1 :: {}", key1);
        log.info("key2 :: {}", key2);
        log.info("key3 :: {}", key3);
        return "test";
    }

    @GetMapping("/api/v1/{path1}/hello4")
    String hello4(@PathVariable String path1, String key1, String key2, String key3) {
        log.info("path1 :: {}", path1);
        log.info("key1 :: {}", key1);
        log.info("key2 :: {}", key2);
        log.info("key3 :: {}", key3);
        return "test";
    }
}
