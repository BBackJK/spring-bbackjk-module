package test.bbackjk.http.spring;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.interfaces.ResponseMapper;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Setter
class RestClientScannerConfigurer implements InitializingBean, ApplicationContextAware, BeanNameAware, BeanDefinitionRegistryPostProcessor {

    private String basePackage;
    private Class<? extends Annotation> annotationClass;
    private ApplicationContext applicationContext;
    private String beanName;

    @Override // BeanDefinitionRegistryPostProcessor 의 implements
    public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
        ClassPathRestClientScanner scanner = new ClassPathRestClientScanner(registry);
        scanner.setBasePackage(this.basePackage);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setHttpAgentBeanList(this.applicationContext.getBeanNamesForType(HttpAgent.class));
        scanner.setResponseMapperBeanDefinitionSet(this.applicationContext.getBeanNamesForType(ResponseMapper.class));
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // NOP
    }

    @Override // InitializingBean 의 implements
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(this.basePackage, "basePackage 값이 비어 있습니다.");
        Objects.requireNonNull(this.annotationClass, "annotationClass 값이 비어 있습니다.");
        Objects.requireNonNull(this.applicationContext, "applicationContext 값이 비어 있습니다.");
        Objects.requireNonNull(this.beanName, "beanName 값이 비어 있습니다.");
    }

    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> prcs = this.applicationContext.getBeansOfType(PropertyResourceConfigurer.class, false, false);
        if (!prcs.isEmpty() && this.applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition restClientScannerBean = ((ConfigurableApplicationContext) this.applicationContext).getBeanFactory().getBeanDefinition(this.beanName);
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, restClientScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }
            this.basePackage = (String) restClientScannerBean.getPropertyValues().getPropertyValue("basePackage").getValue();
        }

        this.basePackage = Optional.ofNullable(this.basePackage).map(getEnv()::resolvePlaceholders).orElse(null);
    }

    private Environment getEnv() {
        return this.applicationContext.getEnvironment();
    }
}
