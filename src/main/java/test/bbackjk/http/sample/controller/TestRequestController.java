package test.bbackjk.http.sample.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import test.bbackjk.http.sample.service.RestService;


@RestController
@RequiredArgsConstructor
public class TestRequestController {

    private final RestService restService;

    @GetMapping("/api/v1/hello")
    ResponseEntity<String> hello() {
        return ResponseEntity.ok(restService.test());
    }
}
