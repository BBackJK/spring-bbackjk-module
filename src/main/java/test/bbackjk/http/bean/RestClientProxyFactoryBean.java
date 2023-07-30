package test.bbackjk.http.bean;

import org.springframework.beans.factory.FactoryBean;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.proxy.RestClientProxyFactory;

public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {

    private final RestClientProxyFactory<T> proxyFactory;
    private final Class<T> restClientInterface;

    public RestClientProxyFactoryBean(Class<T> restClientInterface, RestClientConnectProperties connectProperties) {
        this.restClientInterface = restClientInterface;
        this.proxyFactory = new RestClientProxyFactory<>(restClientInterface, connectProperties);
    }

    @Override
    public T getObject() throws Exception {
        return this.proxyFactory.newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return this.restClientInterface;
    }
}
