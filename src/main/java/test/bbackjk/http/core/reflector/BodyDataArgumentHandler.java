package test.bbackjk.http.core.reflector;

import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.interfaces.RestCallback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class BodyDataArgumentHandler implements ParameterArgumentHandler {

    public BodyDataArgumentHandler() {}

    @Override
    public void handle(Map<String, String> headerValue, Map<String, String> pathValue, Map<String, String> queryValue, List<Object> bodyDataList, Optional<Object> arg) throws RestClientCallException {
        if ( arg.isPresent() ) {
            Object o = arg.get();
            boolean isRestCallback = o instanceof RestCallback;
            if ( isRestCallback ) return;
            bodyDataList.add(o);
        }
    }
}
