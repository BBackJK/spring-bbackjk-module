package test.bbackjk.http.bean;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.exceptions.RestClientCommonException;
import test.bbackjk.http.util.ClassUtils;
import test.bbackjk.http.util.Logs;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Configuration
@ConditionalOnProperty(value = "bbackjk.http.enable", havingValue = "true")
public class RestClientInterfaceBeanRegistrar implements BeanFactoryPostProcessor, EnvironmentAware {

    private Environment environment;

    public RestClientInterfaceBeanRegistrar() {
        Logs.log("RestClient Bean Register ... On ...");
    }

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String basePackage = this.getBasePackageName(beanFactory);
        Set<Class<?>> restClients;

        try {
            restClients = ClassUtils.scanningClassByAnnotation(basePackage, RestClient.class);
        } catch (IOException | ClassNotFoundException e) {
            Logs.err(e.getMessage());
            throw new RestClientCommonException("RestClient 어노테이션 클래스를 스캐닝하는데에 문제가 발생하였습니다.");
        }

        RestClientConnectProperties restClientConnectProperties = new RestClientConnectProperties(this.environment);
        for ( Class<?> restClient : restClients ) {
            String className = restClient.getSimpleName(); // RestClient
            if ( !restClient.isInterface() ) {
                Logs.warn(className + " 클래스는 HttpAgentClient 를 사용할 수 있는 interface 가 아닙니다.");
                continue;
            }

            // 이미 빈을 가지고 있으면 PASS
            boolean hasAlreadyBean = beanFactory.getBeanNamesForType(restClient).length >= 1;
            if ( !hasAlreadyBean ) {
                String restClientBeanName = ClassUtils.toCamel(className); // restClient

                // singleton 등록
                try {
                    beanFactory.registerSingleton(
                            restClientBeanName
                            , Objects.requireNonNull(
                                    new RestClientProxyFactoryBean<>(restClient, restClientConnectProperties).getObject()
                            )
                    );
                } catch (Exception e) {
                    Logs.err(e.getMessage());
                    throw new RestClientCommonException("RestClient Proxy 를 생성하는데에 실패하였습니다.");
                }
            }
        }
    }

    private String getBasePackageName(ConfigurableListableBeanFactory beanFactory) {
        String[] springBootApplicationBeanNames = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
        if ( springBootApplicationBeanNames.length != 1 ) {
            throw new RestClientCommonException("SpringBootApplication 어노테이션을 가진 클래스가 없거나 하나 이상입니다.");
        }
        String springBootApplicationBeanName = springBootApplicationBeanNames[0];
        Object springBootApplication = beanFactory.getBean(springBootApplicationBeanName);
        return springBootApplication.getClass().getPackageName();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
