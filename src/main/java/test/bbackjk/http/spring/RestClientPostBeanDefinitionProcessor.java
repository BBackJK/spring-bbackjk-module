package test.bbackjk.http.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.lang.Nullable;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.bean.agent.OkHttpAgent;
import test.bbackjk.http.core.bean.mapper.DefaultResponseMapper;
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Set;

public class RestClientPostBeanDefinitionProcessor extends AbstractPostBeanDefinitionProcessor {

    private static final LogHelper LOGGER = LogHelper.of(RestClientPostBeanDefinitionProcessor.class);
    private static final ClassLoader[] CLASS_LOADERS = ClassUtil.getClassLoaders();
    private static final String DEFAULT_AGENT_CLASS_NAME = "OkHttpAgent";
    private static final String DEFAULT_MAPPER_CLASS_NAME = "DefaultResponseMapper";

    private final Set<BeanDefinition> httpAgentBeanDefinitionSet;
    private final Set<BeanDefinition> responseMapperBeanDefinitionSet;
    private final BeanDefinition defaultResponseMapperBeanDefinition;
    private final BeanDefinition defaultHttpAgentBeanDefinition;
    private final Class<? extends Annotation> annotationClass;

    public RestClientPostBeanDefinitionProcessor(
            String basePackage
            , Class<? extends Annotation> annotationClass
            , Set<BeanDefinition> httpAgentBeanDefinitionSet
            , Set<BeanDefinition> responseMapperBeanDefinitionSet
    ) {
        this.annotationClass = annotationClass;
        this.httpAgentBeanDefinitionSet = httpAgentBeanDefinitionSet;
        this.responseMapperBeanDefinitionSet = responseMapperBeanDefinitionSet;
        this.defaultHttpAgentBeanDefinition = this.getDefaultHttpAgentBeanDefinition(httpAgentBeanDefinitionSet, basePackage);
        this.defaultResponseMapperBeanDefinition = this.getDefaultResponseMapperBeanDefinition(responseMapperBeanDefinitionSet, basePackage);
        if ( this.defaultHttpAgentBeanDefinition == null || this.defaultResponseMapperBeanDefinition == null ) {
            throw new RestClientCommonException(String.format("%s %s 를 찾을 수 없습니다.", DEFAULT_AGENT_CLASS_NAME, DEFAULT_MAPPER_CLASS_NAME));
        }
    }

    @Override
    protected void postProcess(AbstractBeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        // RestClientProxyFactoryBean 생성자에 Interface Class 주입
        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
        // RestClientProxyFactoryBean 생성자에 HttpAgent Bean 주입
        definition.getConstructorArgumentValues().addGenericArgumentValue(this.findHttpAgentBeanDefinition(beanClassName));
        // RestClientProxyFactoryBean 생성자에 ResponseMapper Bean 주입
        definition.getConstructorArgumentValues().addGenericArgumentValue(this.findResponseMapperBeanDefinition(beanClassName));
    }

    @Nullable
    private BeanDefinition getDefaultResponseMapperBeanDefinition(Set<BeanDefinition> responseMapperBeanDefinitionSet, String basePackage) {
        for (BeanDefinition bd : responseMapperBeanDefinitionSet) {
            String beanClassName = bd.getBeanClassName();
            if ( beanClassName != null && beanClassName.startsWith(basePackage) && beanClassName.endsWith("." + DEFAULT_MAPPER_CLASS_NAME) ) {
                return bd;
            }
        }
        return null;
    }

    @Nullable
    private BeanDefinition getDefaultHttpAgentBeanDefinition(Set<BeanDefinition> httpAgentBeanDefinitionSet, String basePackage) {
        for (BeanDefinition bd : httpAgentBeanDefinitionSet) {
            String beanClassName = bd.getBeanClassName();
            if ( beanClassName != null && beanClassName.startsWith(basePackage) && beanClassName.endsWith("." + DEFAULT_AGENT_CLASS_NAME) ) {
                return bd;
            }
        }
        return null;
    }

    /**
     * HttpAgent 로 이루어진 BeanDefinition 중 해당 RestClient 의 설정으로 설정한 class 와 일치하는 httpAgent 를 가져온다. 없으면 defaultHttpAgent
     */
    private BeanDefinition findHttpAgentBeanDefinition(String restClientBeanClassName) {
        Class<? extends HttpAgent> httpAgentClass = OkHttpAgent.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName);
            if ( restClient != null ) {
                httpAgentClass = restClient.agent();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.err(e.getMessage());
            // ignore..
        }

        BeanDefinition result = this.defaultHttpAgentBeanDefinition;
        for (BeanDefinition def : this.httpAgentBeanDefinitionSet) {
            if (def != null && httpAgentClass.getName().equals(def.getBeanClassName())) {
                result = def;
                break;
            }
        }
        return result;
    }

    /**
     * ResponseMapper 로 이루어진 BeanDefinition 중 해당 RestClient 의 설정으로 설정한 class 와 일치하는 ResponseMapper 를 가져온다. 없으면 defaultResponseMapper
     */
    private BeanDefinition findResponseMapperBeanDefinition(String restClientBeanClassName) {
        Class<? extends ResponseMapper> responseMapperClass = DefaultResponseMapper.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName);
            if ( restClient != null ) {
                responseMapperClass = restClient.mapper();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.err(e.getMessage());
            // ignore..
        }

        BeanDefinition result = this.defaultResponseMapperBeanDefinition;
        for (BeanDefinition def : this.responseMapperBeanDefinitionSet) {
            if (def != null && responseMapperClass.getName().equals(def.getBeanClassName())) {
                result = def;
                break;
            }
        }
        return result;
    }

    /*
     * RestClient Bean Class Name 으로부터 Class 를 가져와서 RestClient Annotation 을 가져온다.
     */
    private RestClient getRestClientAnnotation(String restClientBeanClassName) throws ClassNotFoundException {
        if ( restClientBeanClassName == null || restClientBeanClassName.isEmpty() ) {
            return null;
        }

        Class<?> restClientClass = ClassUtil.classForName(restClientBeanClassName, CLASS_LOADERS);
        Annotation restClientAnnotation = restClientClass.getAnnotation(this.annotationClass);

        if ( restClientAnnotation == null || restClientAnnotation.annotationType() != RestClient.class) {
            LOGGER.err("restClientAnnotation == null 이거나 Annotation 이 RestClient 가 아닙니다.");
            throw new RestClientCommonException("RestClient Bean 을 생성하는데 문제가 발생하였습니다.");
        }

        return (RestClient) restClientAnnotation;
    }
}
