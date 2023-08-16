package test.bbackjk.http.core.proxy;

import org.jetbrains.annotations.Nullable;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.exceptions.RestClientDataMappingException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.interfaces.RestCallback;
import test.bbackjk.http.core.reflector.RequestMethodMetadata;
import test.bbackjk.http.core.util.ClassUtil;
import test.bbackjk.http.core.util.ObjectUtils;
import test.bbackjk.http.core.util.RestMapUtils;
import test.bbackjk.http.core.wrapper.ResponseMetadata;
import test.bbackjk.http.core.wrapper.RestClientInvoker;
import test.bbackjk.http.core.wrapper.RestResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

class RestClientProxy<T> implements InvocationHandler {

    private static final LogHelper LOGGER = LogHelper.of(RestClientProxy.class);
    private final RestClient restClient;
    private final HttpAgent httpAgent;
    private final ResponseMapper dataMapper;
    private final Map<Method, RestClientInvoker> cachedMethod;
    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RestClientInvoker> cachedMethod
            , HttpAgent httpAgent
            , ResponseMapper dataMapper
    ) {
        this.restClient = restClientInterface.getAnnotation(RestClient.class);
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
        this.cachedMethod = cachedMethod;
        this.initCachedMethodHandlerByMethod(restClientInterface.getMethods(), httpAgent);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RestClientInvoker invoker = RestMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RestClientInvoker(m, this.httpAgent));
        ResponseMetadata response;
        try {
            response = invoker.invoke(args, this.restClient.url());
            if ( !response.isSuccess() && !invoker.hasFailHandler() ) {
                throw new RestClientCallException(response.getFailMessage());
            }
        } catch (RestClientCallException e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCallException();
        }

        Object result = null;

        try {
            result = this.toReturnValues(invoker.getMethodMetadata(), response);
        } catch (RestClientDataMappingException e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCallException();
        }

        Object finalResult = result;
        Optional.ofNullable(this.getRestCallbackByArg(args)).ifPresent(callback -> callback.run(response.isSuccess(), response.getHttpCode(), finalResult, response.getFailMessage()));

        return result;
    }

    private void initCachedMethodHandlerByMethod(Method[] methods, HttpAgent httpAgent) {
        if ( this.cachedMethod.isEmpty() && methods != null ) {
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                this.cachedMethod.put(m, new RestClientInvoker(m, httpAgent));
            }
        }
    }

    // TODO : Class Return Type Handler 객체를 추가하여 소스코드 깔끔히..
    private Object toReturnValues(RequestMethodMetadata restClientMethod, ResponseMetadata response) throws RestClientDataMappingException {
        if ( response == null ) {
            return null;
        }

        Object result = null;
        String stringResponse = response.getStringResponse();
        Class<?> returnRawType = restClientMethod.getRawType();

        if (ObjectUtils.isEmpty(stringResponse)) {
            result = ClassUtil.getTypeInitValue(returnRawType);
        } else if (response.isXml()) {
            // xml 일 경우
            result = this.dataMapper.toXml(stringResponse, returnRawType);
        } else if (!restClientMethod.isReturnVoid()) {
            if (restClientMethod.isReturnWrap()) {
                if (restClientMethod.isReturnMap()) {
                    result = this.dataMapper.convert(stringResponse, Map.class);
                } else {
                    result = this.dataMapper.convert(stringResponse, restClientMethod.getSecondRawType(), returnRawType);
                }
            } else {
                if ( restClientMethod.isReturnString() ) {
                    result = stringResponse;
                } else {
                    result = this.dataMapper.convert(stringResponse, returnRawType);
                }
            }
        }

        // wrapping 해주는 return value 들
        if (restClientMethod.isReturnRestResponse()) {
            result = response.isSuccess()
                    ? RestResponse.success(result, response.getHttpCode())
                    : RestResponse.fail(response.getHttpCode(), response.getFailMessage());
        } else if (restClientMethod.isReturnOptional()) {
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
}
