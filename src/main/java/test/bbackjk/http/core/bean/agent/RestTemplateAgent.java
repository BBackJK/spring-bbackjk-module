package test.bbackjk.http.core.bean.agent;

import org.springframework.stereotype.Component;
import test.bbackjk.http.core.configuration.RestClientConnectProperties;
import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.wrapper.RestCommonResponse;


@Component
public class RestTemplateAgent implements HttpAgent {
//    private final OkHttpClient client;
//    private final ObjectMapper om;

    public RestTemplateAgent(RestClientConnectProperties connectProperties) {
    }


    @Override
    public RestCommonResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }
}
