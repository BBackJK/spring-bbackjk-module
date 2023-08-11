package test.bbackjk.http.sample.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import test.bbackjk.http.sample.dto.CorpAffiliateResponse;
import test.bbackjk.http.sample.dto.CorpAffiliateXmlResponse;
import test.bbackjk.http.sample.dto.MemberDto;
import test.bbackjk.http.sample.service.CorpService;
import test.bbackjk.http.sample.service.RestService;


@RestController
@RequiredArgsConstructor
public class TestRequestController {

    private final RestService restService;
    private final CorpService corpService;

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

    @GetMapping("/api/v1/hello/post1")
    ResponseEntity<String> post1() {
        return ResponseEntity.ok(restService.post1());
    }


    @GetMapping("/api/v1/hello/post2")
    ResponseEntity<String> post2() {
        return ResponseEntity.ok(restService.post2());
    }

    @GetMapping("/api/v1/hello/post3")
    ResponseEntity<MemberDto> post3() {
        return ResponseEntity.ok(restService.post3());
    }

    @GetMapping("/api/v1/hello/post31")
    ResponseEntity<MemberDto> post31() {
        return ResponseEntity.ok(restService.post31());
    }

    @GetMapping(value = "/api/v1/hello/post4")
    ResponseEntity<MemberDto> post4() {
        return ResponseEntity.ok(restService.post4());
    }

    @GetMapping(value = "/api/v1/corp1")
    ResponseEntity<CorpAffiliateResponse> corp1() {
        return ResponseEntity.ok(
                this.corpService.getAffiliateJson()
        );
    }

    @GetMapping(value = "/api/v1/corp2")
    ResponseEntity<CorpAffiliateXmlResponse> corp2() {
        return ResponseEntity.ok(
                this.corpService.getAffiliateXml()
        );
    }
}
