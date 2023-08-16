package test.bbackjk.http.sample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.sample.dto.MemberDto;
import test.bbackjk.http.sample.dto.SampleRequestDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class SampleResponseController {

    private final List<MemberDto> dummyMember;

    public SampleResponseController() {
        this.dummyMember = new ArrayList<>();
        for (int i=0; i<10; i++) {
            int val = i+1;
            dummyMember.add(MemberDto.of(val, "이름" + val));
        }
    }

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

    @GetMapping("/api/v1/int")
    Integer intTest() {
        return null;
    }


    @PostMapping("/api/v1/post1")
    String post1(@RequestBody SampleRequestDto requestDto) {
        log.info("requestDto :: {}", requestDto);
        return "Hello! Post!";
    }

    @PostMapping("/api/v1/post2")
    String post2(SampleRequestDto requestDto) {
        log.info("requestDto :: {}", requestDto);
        return "Hello! Post2!";
    }

    @PostMapping("/api/v1/post3")
    ResponseEntity<MemberDto> post3(@RequestBody MemberDto findMemberDto) {
        if ( findMemberDto == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        MemberDto result = this.dummyMember.stream().filter(m -> m.getId() == findMemberDto.getId()).findFirst().orElseGet(() -> null);
        if ( result == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            //throw new RuntimeException("테스트 런타임");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/v1/post4")
    ResponseEntity<List<MemberDto>> post4(@RequestBody MemberDto findMemberDto) {
        MemberDto result = this.dummyMember.stream().filter(m -> m.getId() == findMemberDto.getId()).findFirst().orElseGet(() -> null);
        if ( result == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(this.dummyMember);
    }
}
