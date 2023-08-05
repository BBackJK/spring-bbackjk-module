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
        return ResponseEntity.ok(restService.basic());
    }

    @GetMapping("/api/v1/path")
    ResponseEntity<String> path() {
        return ResponseEntity.ok(restService.path());
    }

    @GetMapping("/api/v1/query1")
    ResponseEntity<String> query1() {
        return ResponseEntity.ok(restService.query1());
    }

    @GetMapping("/api/v1/query2")
    ResponseEntity<String> query2() {
        return ResponseEntity.ok(restService.query2());
    }

    @GetMapping("/api/v1/query3")
    ResponseEntity<String> query3() {
        return ResponseEntity.ok(restService.query3());
    }

    @GetMapping("/api/v1/query4")
    ResponseEntity<String> query4() {
        return ResponseEntity.ok(restService.query4());
    }

    @GetMapping("/api/v1/query5")
    ResponseEntity<String> query5() {
        return ResponseEntity.ok(restService.query5());
    }
}
