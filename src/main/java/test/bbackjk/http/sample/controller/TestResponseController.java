package test.bbackjk.http.sample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestResponseController {

    @GetMapping("/api/v1/hello2")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}
