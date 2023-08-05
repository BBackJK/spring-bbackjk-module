package test.bbackjk.http.bean.agent;

import org.springframework.stereotype.Component;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.wrapper.RequestMetadata;
import test.bbackjk.http.wrapper.RestResponse;


@Component
public class RestTemplateAgent implements HttpAgent {
//    private final OkHttpClient client;
//    private final ObjectMapper om;

    public RestTemplateAgent(RestClientConnectProperties connectProperties) {
    }


    @Override
    public RestResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }
}
