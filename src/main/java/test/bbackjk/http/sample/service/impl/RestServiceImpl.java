package test.bbackjk.http.sample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bbackjk.http.sample.service.RestService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestServiceImpl implements RestService {

//    private final TestClient testClient;
    @Override
    public String hello() {
//        log.info("testClient :: {}", testClient);
        return "hello";
    }
}
