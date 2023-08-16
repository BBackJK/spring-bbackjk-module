package test.bbackjk.http.core.reflector;

import test.bbackjk.http.core.exceptions.RestClientCallException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class HeaderValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public HeaderValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(Map<String, String> headerValue, Map<String, String> pathValue, Map<String, String> queryValue, List<Object> bodyDataList, Optional<Object> arg) throws RestClientCallException {
        arg.ifPresent(o -> headerValue.put(this.metadata.getParamName(), String.valueOf(o)));
    }
}
