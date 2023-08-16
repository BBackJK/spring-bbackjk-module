package test.bbackjk.http.core.wrapper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.reflector.RequestMethodMetadata;
import test.bbackjk.http.core.reflector.ReturnValueResolver;

import java.lang.reflect.Method;

public class RestClientInvoker {
    @Getter
    private final RequestMethodMetadata methodMetadata;
    private final HttpAgent httpAgent;
    private final LogHelper restClientLogger;
    @Getter
    @NotNull
    private final ReturnValueResolver restReturnValueResolver;

    public RestClientInvoker(Method method, HttpAgent httpAgent, ResponseMapper dataMapper) {
        Class<?> restClientInterface = method.getDeclaringClass();
        RestClient restClient = restClientInterface.getAnnotation(RestClient.class);
        this.methodMetadata = new RequestMethodMetadata(method);
        this.httpAgent = httpAgent;
        this.restClientLogger = LogHelper.of(this.getRestClientLogContext(restClientInterface, restClient, method));
        this.restReturnValueResolver = new ReturnValueResolver(this.methodMetadata, dataMapper);
    }

    public ResponseMetadata invoke(Object[] args, String origin) throws RestClientCallException {
        ResponseMetadata result;

        switch (this.methodMetadata.getRequestMethod()) {
            case GET:
                result = httpAgent.doGet(this.methodMetadata.applyArgs(args, this.restClientLogger, origin));
                break;
            case POST:
                result = httpAgent.doPost(this.methodMetadata.applyArgs(args, this.restClientLogger, origin));
                break;
            case PATCH:
                result = httpAgent.doPatch(this.methodMetadata.applyArgs(args, this.restClientLogger, origin));
                break;
            case PUT:
                result = httpAgent.doPut(this.methodMetadata.applyArgs(args, this.restClientLogger, origin));
                break;
            case DELETE:
                result = httpAgent.doDelete(this.methodMetadata.applyArgs(args, this.restClientLogger, origin));
                break;
            default:
                throw new RestClientCallException(String.format(" 지원하지 않은 Request Method 입니다. RequestMethod :: %s", this.methodMetadata.getRequestMethod()));
        }

        return result;
    }

    public boolean hasFailHandler() {
        return this.methodMetadata.isReturnRestResponse() || this.methodMetadata.hasRestCallback() || this.methodMetadata.isReturnOptional();
    }

    @NotNull
    private String getRestClientLogContext(Class<?> restClientInterface, RestClient restClient, Method m) {
        String value = restClient.value();
        String context = restClient.context();

        if (value.isEmpty() && context.isEmpty()) {
            return String.format("%s::%s", restClientInterface.getSimpleName(), m.getName());
        } else {
            if (!value.isEmpty()) {
                return String.format("%s#%s::%s", restClientInterface.getSimpleName(), value, m.getName());
            } else {
                return String.format("%s#%s::%s", restClientInterface.getSimpleName(), context, m.getName());
            }
        }
    }
}
