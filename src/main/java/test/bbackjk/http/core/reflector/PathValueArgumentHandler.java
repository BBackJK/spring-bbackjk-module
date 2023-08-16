package test.bbackjk.http.core.reflector;

import test.bbackjk.http.core.exceptions.RestClientCallException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class PathValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public PathValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(Map<String, String> headerValue, Map<String, String> pathValue, Map<String, String> queryValue, List<Object> bodyDataList, Optional<Object> arg) throws RestClientCallException {
        arg.ifPresent(o -> pathValue.put(this.metadata.getParamName(), String.valueOf(o)));
    }
}
