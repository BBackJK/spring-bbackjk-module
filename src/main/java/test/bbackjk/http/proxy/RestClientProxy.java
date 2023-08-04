package test.bbackjk.http.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MethodInvoker;
import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.interfaces.HttpAgent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class RestClientProxy<T> implements InvocationHandler {

    private final Class<T> restClientInterface;
    private final Map<Method, MethodInvoker> cachedClient;
    private final HttpAgent httpAgent;

    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, MethodInvoker> cachedClient
            , HttpAgent httpAgent
    ) {
        this.restClientInterface = restClientInterface;
        this.cachedClient = cachedClient;
        this.httpAgent = httpAgent;

        log.info("restClientInterface :: {}", restClientInterface);
        log.info("httpAgent :: {}", httpAgent);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return null;
    }
}
