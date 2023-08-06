package test.bbackjk.http.proxy;

import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.exceptions.RestClientDataMappingException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.helper.RestCallback;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.interfaces.ResponseMapper;
import test.bbackjk.http.reflector.RequestReturnResolver;
import test.bbackjk.http.reflector.RestClientMethodInvoker;
import test.bbackjk.http.util.RestMapUtils;
import test.bbackjk.http.wrapper.RestCommonResponse;
import test.bbackjk.http.wrapper.RestResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

class RestClientProxy<T> implements InvocationHandler {

    private final Class<?> restClientInterface;
    private final Map<Method, RestClientMethodInvoker> cachedMethod;
    private final HttpAgent httpAgent;
    private final RestClient restClient;
    private final ResponseMapper dataMapper;
    private final LogHelper logger = LogHelper.of(this.getClass());
    private final LogHelper restClientLogger;

    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RestClientMethodInvoker> cachedMethod
            , HttpAgent httpAgent
            , ResponseMapper dataMapper
    ) {
        this.restClientInterface = restClientInterface;
        this.restClient = restClientInterface.getAnnotation(RestClient.class);
        this.restClientLogger = LogHelper.of(this.getRestClientLogContext(restClientInterface));
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
        this.initCachedMethodHandlerByMethod(restClientInterface, cachedMethod);
        this.cachedMethod = cachedMethod;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RestClientMethodInvoker mi = RestMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RestClientMethodInvoker(m, this.restClientLogger, this.restClientInterface));

        RestCommonResponse response;

        try {
            response = mi.invoke(this.restClient.url(), this.httpAgent, args);
        } catch (RestClientCallException e) {
            this.logger.err(e.getMessage());
            throw new RestClientCallException();
        }

        // 요청 실패 시 throws
        // 1. Return type 이 RestResponse 일시
        // 2. RestCallback 을 사용했을 시
        // 핸들링 할 수 있게 해줌.
        if ( !response.isSuccess() && !mi.getRequestReturnResolver().isReturnRestResponse() && mi.hasRestCallbackArgument() ) {
            throw new RestClientCallException(response.getFailMessage());
        }

        try {
            return this.toReturnValues(mi, response, (RestCallback<Object>) this.getRestCallbackByArg(args));
        } catch (RestClientDataMappingException e) {
            this.logger.err(e.getMessage());
            throw new RestClientDataMappingException("RestClient Response Data Mapping 에 실패 하였습니다.");
        }
    }

    private Object toReturnValues(RestClientMethodInvoker methodInvoker, RestCommonResponse response, RestCallback<Object> callback) throws RestClientDataMappingException {
        if ( response == null ) {
            return null;
        }

        Object result = null;
        String jsonResult = response.getJsonString();
        RequestReturnResolver requestReturnResolver = methodInvoker.getRequestReturnResolver();
        if (requestReturnResolver.isReturnRestResponse()) {
            if (response.isSuccess() ) {
                Object data = requestReturnResolver.isReturnsList()
                        ? this.dataMapper.converts(jsonResult, requestReturnResolver.getReturnRawType())
                        : this.dataMapper.convert(jsonResult, requestReturnResolver.getReturnRawType());
                result = RestResponse.success(data, response.getHttpCode());
            } else {
                result = RestResponse.fail(response.getHttpCode(), response.getFailMessage());
            }
        } else if (requestReturnResolver.isReturnsList()) {
            result = this.dataMapper.converts(jsonResult, requestReturnResolver.getReturnRawType());
        } else if (requestReturnResolver.isReturnsMap()) {
            result = this.dataMapper.convert(jsonResult, Map.class);
        } else if (requestReturnResolver.isReturnsOptional()) {
            result = Optional.ofNullable(this.dataMapper.convert(jsonResult, requestReturnResolver.getReturnRawType()));
        } else if (requestReturnResolver.isReturnsString()){
            result = jsonResult;
        } else {
            if ( !requestReturnResolver.isReturnsVoid() ) {
                result = this.dataMapper.convert(jsonResult, requestReturnResolver.getReturnRawType());
            }
        }

        if ( callback != null ) {
            if (response.isSuccess()) {
                callback.onSuccess(
                        result instanceof RestResponse
                                ? ((RestResponse<?>) result).getHttpCode() : response.getHttpCode()
                        ,
                        result instanceof RestResponse ? ((RestResponse<?>) result).getData() : result
                );
            } else {
                callback.onFailure(response.getHttpCode(), response.getFailMessage());
            }
        }

        return result;
    }

    private void initCachedMethodHandlerByMethod(Class<?> restClientInterface, Map<Method, RestClientMethodInvoker> cachedMethod) {
        if ( cachedMethod.isEmpty() ) {
            Method[] methods = restClientInterface.getMethods();
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                cachedMethod.put(m, new RestClientMethodInvoker(m, this.restClientLogger, this.restClientInterface));
            }
        }
    }

    private String getRestClientLogContext(Class<?> restClientInterface) {
        String value = this.restClient.value();
        String context = this.restClient.context();

        if ( value.isBlank() && context.isBlank() ) {
            return restClientInterface.getSimpleName();
        } else {
            if ( !value.isBlank() ) {
                return String.format("%s#%s", restClientInterface.getSimpleName(), value);
            } else {
                return String.format("%s#%s", restClientInterface.getSimpleName(), context);
            }
        }
    }

    private RestCallback<?> getRestCallbackByArg(Object[] args) {
        int paramCount = args.length;
        for (int i=0; i<paramCount; i++) {
            Object arg = args[i];
            if (arg instanceof RestCallback) {
                return (RestCallback<?>) arg;
            }
        }
        return null;
    }
}
