package test.bbackjk.http.core.proxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.exceptions.RestClientDataMappingException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.reflector.RequestMethodMetadata;
import test.bbackjk.http.core.util.ObjectUtils;
import test.bbackjk.http.core.util.RestMapUtils;
import test.bbackjk.http.core.wrapper.RestCommonResponse;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.interfaces.RestCallback;
import test.bbackjk.http.core.reflector.RequestMethodInvoker;
import test.bbackjk.http.core.wrapper.RestResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

class RestClientProxy<T> implements InvocationHandler {

    private static final LogHelper LOGGER = LogHelper.of(RestClientProxy.class);
    private final Class<T> restClientInterface;
    private final HttpAgent httpAgent;
    private final ResponseMapper dataMapper;
    private final String origin;
    private final LogHelper restClientLogger;
    private final Map<Method, RequestMethodInvoker> cachedMethod;
    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RequestMethodInvoker> cachedMethod
            , HttpAgent httpAgent
            , ResponseMapper dataMapper
    ) {
        RestClient restClient = restClientInterface.getAnnotation(RestClient.class);
        this.origin = restClient.url();
        this.restClientInterface = restClientInterface;
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
        this.cachedMethod = cachedMethod;
        this.restClientLogger = LogHelper.of(this.getRestClientLogContext(restClientInterface, restClient));
        this.initCachedMethodHandlerByMethod(restClientInterface, httpAgent, this.origin, this.restClientLogger);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RequestMethodInvoker mi
                = RestMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RequestMethodInvoker(this.restClientInterface, m, this.httpAgent, this.origin, this.restClientLogger));
        RestCommonResponse response;
        try {
            response = mi.execute(args);
        } catch (RestClientCallException e) {
            LOGGER.err(e.getMessage());
            throw e;
        }

        if ( !response.isSuccess() && !mi.hasFailHandler() ) {
            throw new RestClientCallException(response.getFailMessage());
        }

        try {
            Object result = this.toReturnValues(mi, response);

            RestCallback<Object> callback = this.getRestCallbackByArg(args);
            if ( callback != null ) {
                if ( response.isSuccess() ) {
                    callback.onSuccess(response.getHttpCode(), result);
                } else {
                    callback.onFailure(response.getHttpCode(), response.getFailMessage());
                }
            }

            return result;
        } catch (RestClientDataMappingException e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCallException("RestClient Response Data Mapping 에 실패 하였습니다.");
        }
    }

    private void initCachedMethodHandlerByMethod(Class<?> restClientInterface, HttpAgent httpAgent, String origin, LogHelper restClientLogger) {
        if ( this.cachedMethod.isEmpty() ) {
            Method[] methods = restClientInterface.getMethods();
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                this.cachedMethod.put(m, new RequestMethodInvoker(restClientInterface, m, httpAgent, origin, restClientLogger));
            }
        }
    }

    private Object toReturnValues(RequestMethodInvoker invoker, RestCommonResponse response) throws RestClientDataMappingException {
        if ( response == null ) {
            return null;
        }

        Object result = null;
        String jsonResult = response.getJsonString();
        RequestMethodMetadata restClientMethod = invoker.getMethodMetadata();

        if (restClientMethod.isWrap() && !ObjectUtils.isEmpty(jsonResult)) {
            if (restClientMethod.isWrapList() || restClientMethod.isWrapList(restClientMethod.getSecondRawType())) {
                result = this.dataMapper.converts(jsonResult, restClientMethod.getRawType());
            } else if (restClientMethod.isMap()) {
                result = this.dataMapper.convert(jsonResult, Map.class);
            } else {
                result = this.dataMapper.convert(jsonResult, restClientMethod.getRawType());
            }
        } else {
            if (restClientMethod.isString()) {
                result = jsonResult;
            } else {
                result = restClientMethod.isVoid() || ObjectUtils.isEmpty(jsonResult)
                        ? null
                        : this.dataMapper.convert(jsonResult, restClientMethod.getRawType());
            }
        }

        if (restClientMethod.isWrapRestResponse()) {
            result = response.isSuccess()
                    ? RestResponse.success(result, response.getHttpCode())
                    : RestResponse.fail(response.getHttpCode(), response.getFailMessage());
        } else if (restClientMethod.isWrapOptional()) {
            result = Optional.ofNullable(result);
        }

        return result;
    }

    @Nullable
    private RestCallback<Object> getRestCallbackByArg(Object[] args) {
        if ( args == null ) {
            return null;
        }
        int paramCount = args.length;
        for (int i=0; i<paramCount; i++) {
            Object arg = args[i];
            if (arg instanceof RestCallback) {
                return (RestCallback<Object>) arg;
            }
        }
        return null;
    }

    @NotNull
    private String getRestClientLogContext(Class<?> restClientInterface, RestClient restClient) {
        String value = restClient.value();
        String context = restClient.context();

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
}
