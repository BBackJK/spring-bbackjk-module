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
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.util.BeansUtil;
import test.bbackjk.http.core.util.ClassUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

class ClassPathRestClientScanner extends ClassPathBeanDefinitionScanner {
    private static final LogHelper LOGGER = LogHelper.of(ClassPathRestClientScanner.class);
    private final BeanDefinitionRegistry registry;
    private Set<BeanDefinition> httpAgentBeanDefinitionSet;
    private Set<BeanDefinition> responseMapperBeanDefinitionSet;
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
                httpAgentBeanDefinitions.add(this.registry.getBeanDefinition(httpAgentBeanName));
            }
        }
        this.httpAgentBeanDefinitionSet = httpAgentBeanDefinitions;
    }
    public void setResponseMapperBeanDefinitionSet(String[] responseMapperBeanNames) {
        Set<BeanDefinition> responseMapperBeanDefinitions = new LinkedHashSet<>();
        if (responseMapperBeanNames != null) {
            for ( String responseMapperBeanName : responseMapperBeanNames ) {
                responseMapperBeanDefinitions.add(this.registry.getBeanDefinition(responseMapperBeanName));
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
        AbstractPostBeanDefinitionProcessor beanDefinitionProcessor = new RestClientPostBeanDefinitionProcessor(
                this.basePackage
                , this.annotationClass
                , this.httpAgentBeanDefinitionSet
                , this.responseMapperBeanDefinitionSet
        );
        Set<BeanDefinitionHolder> beanDefinitions = this.doCustomScan(basePackage[0]);
        beanDefinitionProcessor.process(beanDefinitions);
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
     * 검색할 리소스 경로 Find
     */
    private String getPackageSearchPath(String basePackage) {
        return ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtil.convertClassNameToResourcePath(getEnvironment().resolveRequiredPlaceholders(basePackage))
                + "/**/*.class";
    }
}
