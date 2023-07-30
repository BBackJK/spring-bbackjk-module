package test.bbackjk.http.configuration;

import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.interfaces.HttpAgentConfiguration;
import test.bbackjk.http.interfaces.ResponseMapper;
import test.bbackjk.http.mapper.DefaultResponseMapper;

public class DefaultHttpAgentConfiguration implements HttpAgentConfiguration {

    private final HttpAgent httpAgent;
    private final ResponseMapper responseMapper;

    public DefaultHttpAgentConfiguration(HttpAgent httpAgent) {
        this.httpAgent = httpAgent;
        this.responseMapper = new DefaultResponseMapper();
    }

    @Override
    public HttpAgent getHttpAgent() {
        return this.httpAgent;
    }

    @Override
    public ResponseMapper getResponseMapper() {
        return this.responseMapper;
    }
}
