package test.bbackjk.http.spring;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import test.bbackjk.http.core.bean.RestClientProxyFactoryBean;

import java.util.Set;

public abstract class AbstractPostBeanDefinitionProcessor {
    private static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    public void process(Set<BeanDefinitionHolder> beanDefinitions) {
        AbstractBeanDefinition definition;
        for ( BeanDefinitionHolder holder : beanDefinitions ) {
            definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            if ( beanClassName == null || beanClassName.isEmpty() ) continue;

            this.postProcess(definition);

            definition.setBeanClass(RestClientProxyFactoryBean.class);
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            definition.setLazyInit(false);
        }
    }

    protected abstract void postProcess(AbstractBeanDefinition definition);
}
