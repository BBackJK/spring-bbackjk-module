package test.bbackjk.http.core.bean.agent;

import org.springframework.stereotype.Component;
import test.bbackjk.http.core.configuration.RestClientConnectProperties;
import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.wrapper.ResponseMetadata;


@Component
public class RestTemplateAgent implements HttpAgent {
//    private final OkHttpClient client;
//    private final ObjectMapper om;

    public RestTemplateAgent(RestClientConnectProperties connectProperties) {
    }


    @Override
    public ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }
}
