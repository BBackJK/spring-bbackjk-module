package test.bbackjk.http.core.reflector;

import lombok.Getter;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.wrapper.HttpAgentResponse;

import java.lang.reflect.Method;

// TODO: 사용안하는 메소드 정리
public class RequestMethodInvoker {
    @Getter
    private final RequestMethodMetadata methodMetadata;
    private final HttpAgent httpAgent;
    private final LogHelper restClientLogger;

    public RequestMethodInvoker(Class<?> restClientInterface, Method method, HttpAgent httpAgent, String origin, LogHelper restClientLogger) {
        this.methodMetadata = new RequestMethodMetadata(restClientInterface, method, origin);
        this.httpAgent = httpAgent;
        this.restClientLogger = restClientLogger;
    }

    public HttpAgentResponse execute(Object[] args) throws RestClientCallException {
        HttpAgentResponse result;

        switch (this.methodMetadata.getRequestMethod()) {
            case GET:
                result = httpAgent.doGet(this.methodMetadata.applyArgs(args, this.restClientLogger));
                break;
            case POST:
                result = httpAgent.doPost(this.methodMetadata.applyArgs(args, this.restClientLogger));
                break;
            case PATCH:
                result = httpAgent.doPatch(this.methodMetadata.applyArgs(args, this.restClientLogger));
                break;
            case PUT:
                result = httpAgent.doPut(this.methodMetadata.applyArgs(args, this.restClientLogger));
                break;
            case DELETE:
                result = httpAgent.doDelete(this.methodMetadata.applyArgs(args, this.restClientLogger));
                break;
            default:
                throw new RestClientCallException(String.format(" 지원하지 않은 Request Method 입니다. RequestMethod :: %s", this.methodMetadata.getRequestMethod()));
        }

        return result;
    }

    public boolean isXmlAccept() {
        return this.methodMetadata.isXmlAccept();
    }

    public boolean isFormContent() {
        return this.methodMetadata.isFormContent();
    }

    public boolean isJsonContent() {
        return this.methodMetadata.isJsonContent();
    }

    public boolean hasFailHandler() {
        return this.methodMetadata.isReturnRestResponse() || this.methodMetadata.hasRestCallback();
    }
}
