package test.bbackjk.http.bean.agent;

import org.springframework.stereotype.Component;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.wrapper.RequestMetadata;
import test.bbackjk.http.wrapper.RestCommonResponse;


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
