package test.bbackjk.http.core.bean;

import org.springframework.beans.factory.FactoryBean;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.proxy.RestClientProxyFactory;

public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {

    private static final LogHelper LOGGER = LogHelper.of(RestClientProxyFactoryBean.class);
    private final RestClientProxyFactory<T> proxyFactory;
    private final Class<T> restClientInterface;

    public RestClientProxyFactoryBean(Class<T> restClientInterface, HttpAgent httpAgent, ResponseMapper dataMapper) {
        this.restClientInterface = restClientInterface;
        this.proxyFactory = new RestClientProxyFactory<>(this.restClientInterface, httpAgent, dataMapper);
    }

    @Override
    public T getObject() throws Exception {
        T object = this.proxyFactory.newInstance();
        LOGGER.log("RestClient Bean 이 정상적으로 등록되었습니다. 등록된 Class : {}", this.restClientInterface.getName());
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.restClientInterface;
    }
}
