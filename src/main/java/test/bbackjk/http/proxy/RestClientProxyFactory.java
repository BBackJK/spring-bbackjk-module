package test.bbackjk.http.proxy;

import org.springframework.util.MethodInvoker;
import test.bbackjk.http.configuration.RestClientConnectProperties;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RestClientProxyFactory<T> {

    private final Map<Method, MethodInvoker> cachedClient = new ConcurrentHashMap<>();
    private final Class<T> restClientInterface;
    private final RestClientConnectProperties restClientConnectProperties;

    public RestClientProxyFactory(Class<T> restClientInterface, RestClientConnectProperties connectProperties) {
        this.restClientInterface = restClientInterface;
        this.restClientConnectProperties = connectProperties;
    }

    public Class<T> getRestClientInterface() {
        return this.restClientInterface;
    }

    public Map<Method, MethodInvoker> getCachedClient() {
        return this.cachedClient;
    }



    @SuppressWarnings("unchecked")
    protected T newInstance(RestClientProxy<T> restClientProxy) {
        return (T) Proxy.newProxyInstance(
                this.restClientInterface.getClassLoader()
                , new Class[]{ this.restClientInterface }
                , restClientProxy
            );
    }

    public T newInstance() {
        return newInstance(
                new RestClientProxy<>(this.restClientInterface, this.cachedClient, this.restClientConnectProperties)
        );
    }
}
