package test.bbackjk.http.proxy;

import lombok.extern.slf4j.Slf4j;
import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.exceptions.RestClientDataMappingException;
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

@Slf4j
public class RestClientProxy<T> implements InvocationHandler {

    private final Class<T> restClientInterface;
    private final Map<Method, RestClientMethodInvoker> cachedMethod;
    private final HttpAgent httpAgent;
    private final RestClient restClient;
    private final ResponseMapper dataMapper;

    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RestClientMethodInvoker> cachedMethod
            , HttpAgent httpAgent
            , ResponseMapper dataMapper
    ) {
        this.restClientInterface = restClientInterface;
        this.restClient = restClientInterface.getAnnotation(RestClient.class);
        this.cachedMethod = cachedMethod;
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RestClientMethodInvoker mi = RestMapUtils.computeIfAbsent(this.cachedMethod, method, RestClientMethodInvoker::new);

        RestResponse response;

        try {
            response = mi.invoke(this.restClient.url(), this.httpAgent, args);
        } catch (RestClientCallException e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
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
}
