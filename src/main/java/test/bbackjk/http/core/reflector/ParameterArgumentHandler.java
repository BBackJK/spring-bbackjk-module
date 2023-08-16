package test.bbackjk.http.core.reflector;

import test.bbackjk.http.core.exceptions.RestClientCallException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ParameterArgumentHandler {

    void handle(
            Map<String, String> headerValue
            , Map<String, String> pathValue
            , Map<String, String> queryValue
            , List<Object> bodyDataList
            , Optional<Object> arg
    ) throws RestClientCallException;
}
