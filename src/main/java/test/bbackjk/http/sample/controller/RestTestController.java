package test.bbackjk.http.sample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RestTestController {

    @GetMapping("/api/v1/hello")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}
