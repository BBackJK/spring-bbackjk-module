package test.bbackjk.http.sample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.interfaces.RestCallback;
import test.bbackjk.http.sample.dto.MemberDto;
import test.bbackjk.http.sample.dto.SampleRequestDto;
import test.bbackjk.http.sample.http.GetClient;
import test.bbackjk.http.sample.http.PostClient;
import test.bbackjk.http.sample.service.RestService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestServiceImpl implements RestService {

    private final GetClient getClient;
    private final PostClient postClient;

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

    @Override
    public String post1() {
        return this.postClient.post1(
                SampleRequestDto.of("postJson1", "postJson2", "postJson3")
        );
    }

    @Override
    public String post2() {
        return this.postClient.post2(
            SampleRequestDto.of("postForm1", "postForm2", "postForm3")
        );
    }

    @Override
    public MemberDto post3() {
        try {
            this.postClient.post32(
                    MemberDto.of(5, null)
                    , new RestCallback<MemberDto>() {
                        @Override
                        public void onSuccess(int httpCode, MemberDto data) {
                            log.info("code :: {}, data :: {}", httpCode, data);

                            // Do SomeThing...
                        }

                        @Override
                        public void onFailure(int httpCode, String errorMessage) {
                            log.info("code :: {}, errorMessage :: {}", httpCode, errorMessage);

                            // Do SomeThing...
                        }
                    }
            );
            
            return null;
        } catch (RestClientCallException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    public MemberDto post31() {
        return this.postClient.post31(
                        MemberDto.of(14, null)
                )
                .ifSuccess(data -> {
                    log.info("data :: {}", data);
                    // Do SomeThing..
                })
                .ifFailure((code, message) -> {
                    log.info("code :: {}, message :: {}", code, message);
                    // Do SomeThing..
                }).getData();
    }

    @Override
    public MemberDto post4() {
        List<MemberDto> result = this.postClient.post4(
                        MemberDto.of(5, null)
                )
                .ifSuccess(data -> {
                    log.info("data :: {}", data);
                })
                .ifFailure((code, message) -> {
                    log.info("code :: {}, message :: {}", code, message);
                    // Do SomeThing..
                }).getData();
        return result != null && !result.isEmpty()
                ? result.get(0)
                : null;
    }
}
