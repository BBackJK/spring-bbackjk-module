package test.bbackjk.http.core.bean;

import org.springframework.beans.factory.FactoryBean;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.proxy.RestClientProxyFactory;

public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {

    private final RestClientProxyFactory<T> proxyFactory;
    private final Class<T> restClientInterface;

    public RestClientProxyFactoryBean(Class<T> restClientInterface, HttpAgent httpAgent, ResponseMapper dataMapper) {
        this.restClientInterface = restClientInterface;
        this.proxyFactory = new RestClientProxyFactory<>(this.restClientInterface, httpAgent, dataMapper);
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
