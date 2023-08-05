package test.bbackjk.http.sample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bbackjk.http.sample.dto.SampleRequestDto;
import test.bbackjk.http.sample.http.GetClient;
import test.bbackjk.http.sample.service.RestService;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestServiceImpl implements RestService {

    private final GetClient getClient;

    @Override
    public String basic() {
        return getClient.hello();
    }

    @Override
    public String path() {
        return getClient.pathVariableTest("valueTest");
    }

    @Override
    public String query1() {
        return getClient.queryValueEmptyAllAnnotationTest("value1", "value2", "value3");
    }

    @Override
    public String query2() {
        return getClient.queryValueOneAnnotationTest("value3");
    }

    @Override
    public String query3() {
        return getClient.queryObject(SampleRequestDto.of("object1", "object2", "object3"));
    }

    @Override
    public String query4() {
        Map<String, String> request = new HashMap<>();
        request.put("key1", "mapValue1");
        request.put("key2", "mapValue2");
        request.put("key3", "mapValue3");
        return getClient.queryMap(request);
    }

    @Override
    public String query5() {
        return getClient.queryPathHeader(
                "pathValue1234"
                , "Bearer accessToken1234"
                , SampleRequestDto.of("object11", "object22", "object33")
        );
    }
}
