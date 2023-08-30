package test.bbackjk.http.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;

public class AutoConfiguredRestClientScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
    private static final Class<RestClientScannerConfigurer> SCANNER_CONFIGURER_CLASS = RestClientScannerConfigurer.class;
    private static final LogHelper LOGGER = LogHelper.of(AutoConfiguredRestClientScannerRegistrar.class);
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SCANNER_CONFIGURER_CLASS);
        builder.addPropertyValue("basePackage", this.getBasePackageName());
        builder.addPropertyValue("annotationClass", RestClient.class);
        registry.registerBeanDefinition(SCANNER_CONFIGURER_CLASS.getName(), builder.getBeanDefinition());
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    private String getBasePackageName() {
        try {
            String[] springBootApplicationBeanNames = this.beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
            if ( springBootApplicationBeanNames.length != 1 ) {
                throw new RestClientCommonException("SpringBootApplication 어노테이션을 가진 클래스가 없거나 하나 이상입니다.");
            }
            String springBootApplicationBeanName = springBootApplicationBeanNames[0];
            BeanDefinition bd = this.beanFactory.getBeanDefinition(springBootApplicationBeanName);
            return ClassUtils.getPackageName(bd.getResolvableType().toString());
        } catch (Exception e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCommonException("SpringBootApplication 클래스를 찾는데 실패하였습니다.");
        }
    }
}
