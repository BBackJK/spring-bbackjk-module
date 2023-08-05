package test.bbackjk.http.proxy;

import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.exceptions.RestClientDataMappingException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.interfaces.ResponseMapper;
import test.bbackjk.http.reflector.RequestReturnResolver;
import test.bbackjk.http.reflector.RestClientMethodInvoker;
import test.bbackjk.http.util.RestMapUtils;
import test.bbackjk.http.wrapper.RestResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class RestClientProxy<T> implements InvocationHandler {

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
        this.restClient = restClientInterface.getAnnotation(RestClient.class);
        this.restClientLogger = LogHelper.of(this.getRestClientLogContext(restClientInterface));
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
        this.cachedMethodHandlerByMethod(restClientInterface, cachedMethod);
        this.cachedMethod = cachedMethod;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RestClientMethodInvoker mi = RestMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RestClientMethodInvoker(m, this.restClientLogger));

        RestResponse response;

        try {
            response = mi.invoke(this.restClient.url(), this.httpAgent, args);
        } catch (RestClientCallException e) {
            this.logger.err(e.getMessage());
            throw new RestClientCallException();
        }

        // 요청 실패 시 throws
        // TODO: 추후 Return type 이 일반 객체가 아닌, 요청 RestResponse 를 래핑한 값이면, 핸들링 할 수 있게 처리
        // TODO: 혹은, Callback 을 인자로 받아서, 요청 실패에 대해 핸들링 할 수 있게 하려고함.
        if ( !response.isSuccess() ) {
            throw new RestClientCallException(response.getMessage());
        }

        try {
            return this.toReturnValues(mi, response.getJsonString());
        } catch (RestClientDataMappingException e) {
            this.logger.err(e.getMessage());
            throw new RestClientDataMappingException("RestClient Response Data Mapping 에 실패 하였습니다.");
        }
    }

    private Object toReturnValues(RestClientMethodInvoker methodInvoker, String result) throws RestClientDataMappingException {
        if ( result == null ) {
            return null;
        }

        RequestReturnResolver requestReturnResolver = methodInvoker.getRequestReturnResolver();
        if (requestReturnResolver.isReturnsList()) {
            return this.dataMapper.converts(result, requestReturnResolver.getReturnRawType());
        } else if (requestReturnResolver.isReturnsMap()) {
            return this.dataMapper.convert(result, Map.class);
        } else if (requestReturnResolver.isReturnsOptional()) {
            return Optional.ofNullable(this.dataMapper.convert(result, requestReturnResolver.getReturnRawType()));
        } else if (requestReturnResolver.isReturnsVoid()) {
            return null;
        } else if (requestReturnResolver.isReturnsString()){
            return result;
        } else {
            return this.dataMapper.convert(result, requestReturnResolver.getReturnRawType());
        }
    }

    private void cachedMethodHandlerByMethod(Class<?> restClientInterface, Map<Method, RestClientMethodInvoker> cachedMethod) {
        if ( cachedMethod.isEmpty() ) {
            Method[] methods = restClientInterface.getMethods();
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                cachedMethod.put(m, new RestClientMethodInvoker(m, this.restClientLogger));
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
}
