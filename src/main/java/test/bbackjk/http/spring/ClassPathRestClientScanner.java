package test.bbackjk.http.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import test.bbackjk.http.core.annotations.RestClient;
import test.bbackjk.http.core.bean.RestClientProxyFactoryBean;
import test.bbackjk.http.core.bean.agent.OkHttpAgent;
import test.bbackjk.http.core.bean.mapper.DefaultResponseMapper;
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.interfaces.ResponseMapper;
import test.bbackjk.http.core.util.BeansUtil;
import test.bbackjk.http.core.util.ClassUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

class ClassPathRestClientScanner extends ClassPathBeanDefinitionScanner {

    private static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";
    private static final String DEFAULT_AGENT_CLASS_NAME = "OkHttpAgent";
    private static final String DEFAULT_MAPPER_CLASS_NAME = "DefaultResponseMapper";
    private static final LogHelper LOGGER = LogHelper.of(ClassPathRestClientScanner.class);
    private final BeanDefinitionRegistry registry;
    private Set<BeanDefinition> httpAgentBeanDefinitionSet;
    private Set<BeanDefinition> responseMapperBeanDefinitionSet;
    private BeanDefinition defaultResponseMapperBeanDefinition;
    private BeanDefinition defaultHttpAgentBeanDefinition;
    private Class<? extends Annotation> annotationClass;
    private String basePackage;

    public ClassPathRestClientScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        this.registry = registry;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }
    public void setHttpAgentBeanList(String[] httpAgentBeanNames) {
        Set<BeanDefinition> httpAgentBeanDefinitions = new LinkedHashSet<>();
        if (httpAgentBeanNames != null) {
            for ( String httpAgentBeanName : httpAgentBeanNames ) {
                BeanDefinition bd = this.registry.getBeanDefinition(httpAgentBeanName);
                String beanClassName = bd.getBeanClassName();
                if ( beanClassName != null && beanClassName.startsWith(this.basePackage) && beanClassName.endsWith("." + DEFAULT_AGENT_CLASS_NAME) ) {
                    this.defaultHttpAgentBeanDefinition = bd;
                }
                httpAgentBeanDefinitions.add(bd);
            }
        }
        this.httpAgentBeanDefinitionSet = httpAgentBeanDefinitions;
    }
    public void setResponseMapperBeanDefinitionSet(String[] responseMapperBeanNames) {
        Set<BeanDefinition> responseMapperBeanDefinitions = new LinkedHashSet<>();
        if (responseMapperBeanNames != null) {
            for ( String responseMapperBeanName : responseMapperBeanNames ) {
                BeanDefinition bd = this.registry.getBeanDefinition(responseMapperBeanName);
                String beanClassName = bd.getBeanClassName();
                if ( beanClassName != null && beanClassName.startsWith(this.basePackage) && beanClassName.endsWith("." + DEFAULT_MAPPER_CLASS_NAME) ) {
                    this.defaultResponseMapperBeanDefinition = bd;
                }
                responseMapperBeanDefinitions.add(bd);
            }
        }
        this.responseMapperBeanDefinitionSet = responseMapperBeanDefinitions;
    }
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * ClassPathBeanDefinitionScanner 의 scan 함수에서 해당 doScan 을 실행
     */
    @NotNull
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackage) {
        Set<BeanDefinitionHolder> beanDefinitions = this.doCustomScan(basePackage[0]);
        this.processBeanDefinitions(beanDefinitions);
        return beanDefinitions;
    }

    /**
     * Annotation 의 TypeFilter Add
     */
    public void registerFilters() {
        Objects.requireNonNull(this.annotationClass, "annotationClass 는 필수값 입니다.");
        addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
    }

    /**
     * 기존 ClassPathBeanDefinitionScanner 를 참조한 Custom Scan
     */
    private Set<BeanDefinitionHolder> doCustomScan(String basePackage) {
        final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
        final BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;
        
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
        Assert.notNull(this.registry, "BeanDefinitionRegistry 은 Null 이 될 수 없습니다.");
        // Filter 에서 걸러진 Class 파일들을 BeanDefinition 으로 만듦.
        Set<BeanDefinition> beanDefinitionSet = this.customScanCandidateComponents(basePackage);
        for ( BeanDefinition def : beanDefinitionSet ) {
            if ( def == null ) continue;
            // Scope 지정
            ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(def);
            def.setScope(scopeMetadata.getScopeName());
            // beanName parsing
            String beanName = beanNameGenerator.generateBeanName(def, this.registry);

            if ( def instanceof AbstractBeanDefinition ) {
                super.postProcessBeanDefinition((AbstractBeanDefinition) def, beanName);
            }

            if ( def instanceof AnnotatedBeanDefinition ) {
                AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) def);
            }

            // registry 에 이미 등록된 bean definition 인지 확인
            if ( super.checkCandidate(beanName, def) ) {
                // BeanDefinitionHolder 생성
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(def, beanName);
                definitionHolder = BeansUtil.applyScopedProxyMode(scopeMetadata, definitionHolder, registry);
                beanDefinitions.add(definitionHolder); // holder 추가
                if ( definitionHolder != null ) {
                    // bean definition 등록
                    super.registerBeanDefinition(definitionHolder, this.registry);
                }
            }
        }
        return beanDefinitions;
    }

    /**
     * basePackage 기반으로 모든 package 안에 있는 class 파일들을 Bean Candidate(빈 후보자) 로 인식.
     * super.isCandidateComponent 를 통하여 등록한 filter 에 만족하는 class 파일들을 후보자로 선정.
     * @param basePackage
     * @return
     */
    private Set<BeanDefinition> customScanCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();

        String packageSearchPath = this.getPackageSearchPath(basePackage);
        LOGGER.log("packageSearchPath : {}", packageSearchPath);
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

        try {
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for ( Resource res : resources ) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(res);
                if ( super.isCandidateComponent(metadataReader) ) {
                    ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                    sbd.setSource(res);
                    candidates.add(sbd);
                }
            }
        } catch (IOException e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCommonException("RestClient 리소스를 읽다가 실패하였습니다.");
        }

        return candidates;
    }

    /**
     * TODO: Registrar 클래스로 빼버리기
     * 등록한 BeanDefinition 후처리
     * @param beanDefinitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        AbstractBeanDefinition definition;
        ClassLoader[] classLoaders = ClassUtil.getClassLoaders();
        for ( BeanDefinitionHolder holder : beanDefinitions ) {
            definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            if ( beanClassName == null || beanClassName.isBlank() ) continue;

            // RestClientProxyFactoryBean 생성자에 Interface Class 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            // RestClientProxyFactoryBean 생성자에 HttpAgent Bean 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(this.findHttpAgentBeanDefinition(beanClassName, classLoaders));
            // RestClientProxyFactoryBean 생성자에 ResponseMapper Bean 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(this.findResponseMapperBeanDefinition(beanClassName, classLoaders));

            definition.setBeanClass(RestClientProxyFactoryBean.class);
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            definition.setLazyInit(false);
        }
    }

    /**
     * 검색할 리소스 경로 Find
     */
    private String getPackageSearchPath(String basePackage) {
        return ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtil.convertClassNameToResourcePath(getEnvironment().resolveRequiredPlaceholders(basePackage))
                + "/**/*.class";
    }

    /**
     * HttpAgent 로 이루어진 BeanDefinition 중 해당 RestClient 의 설정으로 설정한 class 와 일치하는 httpAgent 를 가져온다. 없으면 defaultHttpAgent
     */
    private BeanDefinition findHttpAgentBeanDefinition(String restClientBeanClassName, ClassLoader[] classLoaders) {
        Class<? extends HttpAgent> httpAgentClass = OkHttpAgent.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName, classLoaders);
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
    private BeanDefinition findResponseMapperBeanDefinition(String restClientBeanClassName, ClassLoader[] classLoaders) {
        Class<? extends ResponseMapper> responseMapperClass = DefaultResponseMapper.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName, classLoaders);
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

    /**
     * RestClient Bean Class Name 으로부터 Class 를 가져와서 RestClient Annotation 을 가져온다.
     */
    private RestClient getRestClientAnnotation(String restClientBeanClassName, ClassLoader[] classLoaders) throws ClassNotFoundException {
        if ( restClientBeanClassName == null || restClientBeanClassName.isBlank() ) {
            return null;
        }

        Class<?> restClientClass = ClassUtil.classForName(restClientBeanClassName, classLoaders);
        Annotation restClientAnnotation = restClientClass.getAnnotation(this.annotationClass);

        if ( restClientAnnotation == null || restClientAnnotation.annotationType() != RestClient.class) {
            LOGGER.err("restClientAnnotation == null 이거나 Annotation 이 RestClient 가 아닙니다.");
            throw new RestClientCommonException("RestClient Bean 을 생성하는데 문제가 발생하였습니다.");
        }

        return (RestClient) restClientAnnotation;
    }
}
