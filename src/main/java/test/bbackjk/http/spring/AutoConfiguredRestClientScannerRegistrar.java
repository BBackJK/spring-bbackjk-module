package test.bbackjk.http.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import test.bbackjk.http.annotations.RestClient;
import test.bbackjk.http.exceptions.RestClientCommonException;
import test.bbackjk.http.helper.LogHelper;

public class AutoConfiguredRestClientScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
    private static final Class<RestClientScannerConfigurer> REST_CLIENT_SCANNER_CONFIGURER_CLASS = RestClientScannerConfigurer.class;
    private static final Class<RestClient> TARGET_ANNOTATION_CLASS = RestClient.class;
    private static final String STR_BASE_PACKAGE = "basePackage";
    private static final String STR_ANNOTATION_CLASS = "annotationClass";
    private final LogHelper logger = LogHelper.of(getClass());
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(@NotNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(REST_CLIENT_SCANNER_CONFIGURER_CLASS);
        builder.addPropertyValue(STR_BASE_PACKAGE, this.getBasePackageName());
        builder.addPropertyValue(STR_ANNOTATION_CLASS, TARGET_ANNOTATION_CLASS);
        registry.registerBeanDefinition(REST_CLIENT_SCANNER_CONFIGURER_CLASS.getName(), builder.getBeanDefinition());
    }

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    private String getBasePackageName() {
        try {
            String[] springBootApplicationBeanNames = this.beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
            if ( springBootApplicationBeanNames.length != 1 ) {
                throw new RestClientCommonException("SpringBootApplication 어노테이션을 가진 클래스가 없거나 하나 이상입니다.");
            }
            String springBootApplicationBeanName = springBootApplicationBeanNames[0];
            BeanDefinition bd = this.beanFactory.getBeanDefinition(springBootApplicationBeanName);
            return bd.getResolvableType().resolve().getPackageName();
        } catch (Exception e) {
            this.logger.err(e.getMessage());
            throw new RestClientCommonException("SpringBootApplication 클래스를 찾는데 실패하였습니다.");
        }
    }
}
