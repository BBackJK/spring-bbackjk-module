package test.bbackjk.http.reflector;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.util.ReflectorUtils;
import test.bbackjk.http.wrapper.RestResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class RestClientMethodInvoker {

    private static final List<Class<? extends Annotation>> ALLOWED_REQUEST_MAPPING_ANNOTATIONS;
    private static final String ANNO_METHOD_NAME_METHOD = "method";
    private static final String ANNO_METHOD_NAME_VALUE = "value";
    private static final String ANNO_METHOD_NAME_CONSUMES = "consumes";

    @Getter
    private final Method method;
    @Getter
    private final RequestMethod requestMethod;
    @Getter
    private final String requestPathname;
    @Getter
    private final MediaType contentType;
    @Getter
    private final RequestReturnResolver requestReturnResolver;
    private final RequestMethodMetadata requestMethodMetadata;

    static {
        ALLOWED_REQUEST_MAPPING_ANNOTATIONS = Stream.of(
                RequestMapping.class, GetMapping.class, PostMapping.class
                , PatchMapping.class, PutMapping.class, DeleteMapping.class
        ).collect(Collectors.toUnmodifiableList());
    }

    public RestClientMethodInvoker(Method method) {
        this.method = method;
        Annotation httpMappingAnnotation = this.parseRequestAnnotationByMethod(method);
        this.requestMethod = this.parseRequestMethodByAnnotation(httpMappingAnnotation);
        this.requestPathname = this.parseRequestUrlByAnnotation(httpMappingAnnotation);
        this.contentType = this.parseContentTypeByAnnotation(httpMappingAnnotation);
        this.requestReturnResolver = new RequestReturnResolver(method);
        this.requestMethodMetadata = new RequestMethodMetadata(this);

    }

    public RestResponse invoke(String origin, HttpAgent httpAgent, Object[] args) throws RestClientCallException {
        RestResponse result;

        switch (this.requestMethod) {
            case GET:
                result = httpAgent.doGet(this.requestMethodMetadata.applyArgs(origin, requestPathname, contentType, args));
                break;
            case POST:
                result = httpAgent.doPost(this.requestMethodMetadata.applyArgs(origin, requestPathname, contentType, args));
                break;
            case PATCH:
                result = httpAgent.doPatch(this.requestMethodMetadata.applyArgs(origin, requestPathname, contentType, args));
                break;
            case PUT:
                result = httpAgent.doPut(this.requestMethodMetadata.applyArgs(origin, requestPathname, contentType, args));
                break;
            case DELETE:
                result = httpAgent.doDelete(this.requestMethodMetadata.applyArgs(origin, requestPathname, contentType, args));
                break;
            default:
                throw new RestClientCallException(String.format(" 지원하지 않은 Request Method 입니다. RequestMethod :: %s", this.requestMethod));
        }

        return result;
    }

    public boolean isXmlContent() {
        return MediaType.APPLICATION_XML.equalsTypeAndSubtype(this.contentType);
    }

    public boolean isFormContent() {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(this.contentType);
    }

    @Nullable
    private Annotation parseRequestAnnotationByMethod(Method method) {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (ALLOWED_REQUEST_MAPPING_ANNOTATIONS.contains(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    private RequestMethod parseRequestMethodByAnnotation(@Nullable Annotation annotation) {
        RequestMethod rm = RequestMethod.GET;
        if ( annotation == null ) {
            return rm;
        }

        Class<? extends Annotation> mappingAnnotationClazz = annotation.annotationType();

        if ( mappingAnnotationClazz == RequestMapping.class ) {
            RequestMethod[] requestMethods = (RequestMethod[]) ReflectorUtils.annotationMethodInvoke(annotation, ANNO_METHOD_NAME_METHOD);
            if ( requestMethods != null && requestMethods.length > 0) {
                rm = requestMethods[0];
            }
        } else if ( mappingAnnotationClazz == PostMapping.class ) {
            rm = RequestMethod.POST;
        } else if ( mappingAnnotationClazz == PatchMapping.class ) {
            rm = RequestMethod.PATCH;
        } else if ( mappingAnnotationClazz == PutMapping.class ) {
            rm = RequestMethod.PUT;
        } else if ( mappingAnnotationClazz == DeleteMapping.class ) {
            rm = RequestMethod.DELETE;
        }

        return rm;
    }

    private String parseRequestUrlByAnnotation(@Nullable Annotation annotation) {
        String url = "";
        if ( annotation == null ) {
            return url;
        }
        String[] urlValues = (String[]) ReflectorUtils.annotationMethodInvoke(annotation, ANNO_METHOD_NAME_VALUE);
        return urlValues != null && urlValues.length > 0 ? urlValues[0] : url;
    }

    private MediaType parseContentTypeByAnnotation(@Nullable Annotation annotation) {
        MediaType defaultContentType = MediaType.APPLICATION_JSON;
        if ( annotation == null ) {
            return defaultContentType;
        }
        String[] contentTypeValues = (String[]) ReflectorUtils.annotationMethodInvoke(annotation, ANNO_METHOD_NAME_CONSUMES);
        if ( contentTypeValues == null || contentTypeValues.length < 1 ) {
            return defaultContentType;
        }

        String firstContentType = contentTypeValues[0];
        String[] contentTypeSplit = firstContentType.split("/");
        try {
            return new MediaType(contentTypeSplit[0], contentTypeSplit[1]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            log.warn("Annotation 으로부터 contentType 을 파싱하다 실패하였습니다. 원인 :: {}", e.getMessage());
            return defaultContentType;
        }
    }

}
