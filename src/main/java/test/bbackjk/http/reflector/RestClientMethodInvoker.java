package test.bbackjk.http.reflector;

import lombok.Getter;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.wrapper.RestCommonResponse;

import java.lang.reflect.Method;

public class RestClientMethodInvoker {
    @Getter
    private final RestClientMethodMetadata methodMetadata;
    private final HttpAgent httpAgent;
    private final LogHelper restClientLogger;

    public RestClientMethodInvoker(Class<?> restClientInterface, Method method, HttpAgent httpAgent, String origin, LogHelper restClientLogger) {
        this.methodMetadata = new RestClientMethodMetadata(restClientInterface, method, origin);
        this.httpAgent = httpAgent;
        this.restClientLogger = restClientLogger;
    }

    public RestCommonResponse execute(Object[] args) throws RestClientCallException {
        RestCommonResponse result;

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
        return this.methodMetadata.isWrapRestResponse() || this.hasRestCallback();
    }

    private boolean hasRestCallback() {
        return this.methodMetadata.hasRestCallback();
    }
}
