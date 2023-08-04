package test.bbackjk.http.bean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import test.bbackjk.http.proxy.RestClientProxyFactory;

@Slf4j
public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {

    private final RestClientProxyFactory<T> proxyFactory;
    private Class<T> restClientInterface;

    public RestClientProxyFactoryBean(Class<T> restClientInterface) {
        this.restClientInterface = restClientInterface;
        this.proxyFactory = new RestClientProxyFactory<>(this.restClientInterface);
    }

    public void setRestClientInterface(Class<T> restClientInterface) {
        this.restClientInterface = restClientInterface;
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
