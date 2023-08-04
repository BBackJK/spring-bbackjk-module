package test.bbackjk.http.bean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.proxy.RestClientProxyFactory;

@Slf4j
public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {

    private final RestClientProxyFactory<T> proxyFactory;
    private final Class<T> restClientInterface;
    private final HttpAgent httpAgent;

    public RestClientProxyFactoryBean(Class<T> restClientInterface, HttpAgent httpAgent) {
        this.restClientInterface = restClientInterface;
        this.httpAgent = httpAgent;
        this.proxyFactory = new RestClientProxyFactory<>(this.restClientInterface, this.httpAgent);
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
