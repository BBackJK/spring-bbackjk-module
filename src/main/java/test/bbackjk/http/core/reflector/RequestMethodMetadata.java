package test.bbackjk.http.core.reflector;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.util.ClassUtil;
import test.bbackjk.http.core.util.ReflectorUtils;
import test.bbackjk.http.core.util.RestMapUtils;
import test.bbackjk.http.core.wrapper.RequestMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestMethodMetadata {
    private static final List<Class<? extends Annotation>> ALLOWED_REQUEST_MAPPING_ANNOTATIONS = Collections.unmodifiableList(Stream.of(RequestMapping.class, GetMapping.class, PostMapping.class, PatchMapping.class, PutMapping.class, DeleteMapping.class).collect(Collectors.toList()));
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[a-z|0-9]+}");
    private static final LogHelper LOGGER = LogHelper.of(RequestMethodMetadata.class);

    // Method 어노테이션 :: ALLOWED_REQUEST_MAPPING_ANNOTATIONS 중 하나
    private final Annotation annotation;

    // k :: argument 순서, v :: argument 메타데이터 핸들러
    private final Map<Integer, ParameterArgumentHandler> parameterArgumentHandlerMap;

    // RestCallback 인 argument 메타데이터 목록
    private final List<RequestParamMetadata> restCallbackParameterList;

    // Method 가 RequestParam 어노테이션을 가지고 있는지.
    private final boolean hasRequestParamAnnotation;

    // Method 의 모든 Argument 가 어노테이션이 하나도 없는지.
    private final boolean emptyAllParameterAnnotation;

    // Method 의 Return Metadata
    private final RequestReturnMetadata returnMetadata;

    // Method 의 Http Method
    @Getter
    private final RequestMethod requestMethod;

    // Method 의 pathname
    private final String pathname;

    // Method 의 consumes 로 파싱한 contentType
    private final MediaType contentType;

    // PATH_VARIABLE_PATTERN 으로 찾은 argument name 목록
    private final List<String> pathValueNames;

//    private final ParameterArgumentHandler argumentHandler;

    private final Map<String, String> headerValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> pathValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> queryValuesMap = new ConcurrentHashMap<>();

    public RequestMethodMetadata(Method method) {
        Class<?> restClientInterface = method.getDeclaringClass();
        Map<Integer, RequestParamMetadata> parameterMetadataMap = RestMapUtils.toReadonly(this.getParamMetadataList(method.getParameters()));
        this.annotation = this.parseAnnotation(method);
        this.hasRequestParamAnnotation = parameterMetadataMap.values().stream().anyMatch(RequestParamMetadata::isAnnotationRequestParam);
        this.emptyAllParameterAnnotation = parameterMetadataMap.values().stream().noneMatch(RequestParamMetadata::hasAnnotation);
        this.restCallbackParameterList = Collections.unmodifiableList(parameterMetadataMap.values().stream().filter(RequestParamMetadata::isRestCallback).collect(Collectors.toList()));
        this.requestMethod = this.parseRequestMethodByAnnotation(this.annotation);
        this.pathname = this.parsePathNameByAnnotation(this.annotation);
        this.contentType = this.parseContentTypeByAnnotation(this.annotation);
        this.pathValueNames = this.getPathVariableNames(this.pathname);
        this.returnMetadata = new RequestReturnMetadata(method);
        this.parameterArgumentHandlerMap = parameterMetadataMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey
                        , entry -> ParameterArgumentHandlerFactory.getHandler(entry.getValue(), this.isOnlyRequestParam(), emptyAllParameterAnnotation, pathValueNames)
                ));

        this.valid(this.getValidErrorContext(restClientInterface, method), parameterMetadataMap);
    }

    public boolean isCanHasRequestBodyAnnotation() {
        return this.requestMethod == RequestMethod.POST ||
                this.requestMethod == RequestMethod.PUT ||
                this.requestMethod == RequestMethod.PATCH;
    }

    public boolean hasRestCallback() {
        return !this.restCallbackParameterList.isEmpty();
    }

    public boolean isFormContent() {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(this.contentType);
    }

    public boolean isHasPathValue() {
        return !pathValueNames.isEmpty();
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public RequestMetadata applyArgs(Object[] args, LogHelper restClientLogger, String origin) {
        if ( args == null || args.length == 0 ) {
            return RequestMetadata.of(this.getRequestUrl(origin, this.pathname), this.contentType, restClientLogger);
        }

        int argCount = args.length;
        if ( argCount != this.parameterArgumentHandlerMap.size() ) {
            throw new RestClientCallException();
        }

        List<Object> requestBodyList = new ArrayList<>();
        for (int i=0; i<argCount; i++) {
            Optional<Object> arg = Optional.ofNullable(args[i]);
            ParameterArgumentHandler handler = this.parameterArgumentHandlerMap.get(i);
            if ( handler != null ) {
                handler.handle(
                        this.headerValuesMap
                        , this.pathValuesMap
                        , this.queryValuesMap
                        , requestBodyList
                        , arg
                );
            }
        }

        if ( requestBodyList.size() > 1 ) {
            LOGGER.warn("Request Body 로 인식되는 파라미터가 1개 이상입니다.");
            LOGGER.warn("Request Body : {}", requestBodyList);
        }

        return RequestMetadata.of(
                this.getRequestUrl(origin, this.pathname)
                , this.contentType
                , this.headerValuesMap
                , this.pathValuesMap
                , this.queryValuesMap
                , requestBodyList.isEmpty() ? null : requestBodyList.get(requestBodyList.size() - 1)
                , args
                , restClientLogger);
    }

    public boolean isReturnWrap() {
        return this.returnMetadata.isWrap();
    }

    public boolean isReturnMap() {
        return this.returnMetadata.isWrapMap();
    }

    public boolean isReturnString() {
        return this.returnMetadata.isString();
    }

    public boolean isReturnVoid() {
        return this.returnMetadata.isVoid();
    }

    public boolean isReturnOptional() {
        return this.returnMetadata.isWrapOptional();
    }

    public boolean isReturnRestResponse() {
        return this.returnMetadata.isWrapRestResponse();
    }

    @Nullable
    public Class<?> getSecondRawType() {
        return this.returnMetadata.getSecondRawType();
    }

    public Class<?> getRawType() {
        return this.returnMetadata.getRawType();
    }

    @NotNull
    private List<String> getPathVariableNames(String pathname) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pathname);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String values = matcher.group(); // "{values}"
            result.add(values.substring(1, values.length()-1)); // "values"
        }
        return result;
    }

    @NotNull
    private Map<Integer, RequestParamMetadata> getParamMetadataList(Parameter[] parameters) {
        if ( parameters == null ) {
            return Collections.emptyMap();
        }
        Map<Integer, RequestParamMetadata> result = new LinkedHashMap<>();
        int paramCount = parameters.length;
        for (int i=0; i<paramCount;i++) {
            result.put(i, new RequestParamMetadata(parameters[i]));
        }
        return result;
    }

    @Nullable
    private Annotation parseAnnotation(Method method) {
        for (Class<? extends Annotation> a :  ALLOWED_REQUEST_MAPPING_ANNOTATIONS) {
            Annotation anno = method.getAnnotation(a);
            if ( anno != null ) {
                return anno;
            }
        }
        return null;
    }
    
    @NotNull
    private RequestMethod parseRequestMethodByAnnotation(@Nullable Annotation annotation) {
        RequestMethod rm = RequestMethod.GET;
        if ( annotation == null ) {
            return rm;
        }

        Class<? extends Annotation> mappingAnnotationClazz = annotation.annotationType();

        if ( RequestMapping.class.equals(mappingAnnotationClazz)) {
            RequestMethod[] requestMethods = (RequestMethod[]) ReflectorUtils.annotationMethodInvoke(annotation, "method");
            if ( requestMethods != null && requestMethods.length > 0) {
                rm = requestMethods[0];
            }
        } else if ( PostMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.POST;
        } else if ( PatchMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.PATCH;
        } else if ( PutMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.PUT;
        } else if ( DeleteMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.DELETE;
        }

        return rm;
    }
    
    @NotNull
    private String parsePathNameByAnnotation(@Nullable Annotation annotation) {
        String url = "";
        if ( annotation == null ) {
            return url;
        }
        String[] urlValues = (String[]) ReflectorUtils.annotationMethodInvoke(annotation, "value");
        return urlValues != null && urlValues.length > 0 ? urlValues[0] : url;
    }

    @NotNull
    private String getRequestUrl(String origin, String pathname) {
        String copyOrigin = origin == null ? "" : origin;
        String copyPathname = pathname == null ? "" : pathname;

        if (copyOrigin.endsWith("/")) {
            if (copyPathname.startsWith("/")) {
                return copyOrigin + copyPathname.substring(1);
            } else {
                return copyOrigin + copyPathname;
            }
        } else {
            if (copyPathname.startsWith("/")) {
                return copyOrigin + copyPathname;
            } else {
                if ( copyOrigin.isEmpty() && copyPathname.isEmpty() ) {
                    return "";
                } else {
                    return copyOrigin + "/" + copyPathname;
                }
            }
        }
    }

    @NotNull
    private MediaType parseContentTypeByAnnotation(@Nullable Annotation annotation) {
        MediaType defaultContentType = MediaType.APPLICATION_JSON;
        if ( annotation == null ) {
            return defaultContentType;
        }
        String[] contentTypeValues = (String[]) ReflectorUtils.annotationMethodInvoke(annotation, "consumes");
        if ( contentTypeValues == null || contentTypeValues.length < 1 ) {
            return defaultContentType;
        }

        String firstContentType = contentTypeValues[0];
        String[] contentTypeSplit = firstContentType.split("/");
        try {
            return new MediaType(contentTypeSplit[0], contentTypeSplit[1]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LOGGER.warn("Annotation 으로부터 contentType 을 파싱하다 실패하였습니다. 원인 :: {}", e.getMessage());
            return defaultContentType;
        }
    }

    private boolean isOnlyRequestParam() {
        return (this.isFormContent() && this.isCanHasRequestBodyAnnotation()) || this.hasRequestParamAnnotation;
    }

    private String getValidErrorContext(Class<?> restClientInterface, Method m) {
        return String.format("%s#%s", restClientInterface.getSimpleName(), m.getName());
    }

    private void valid(String errorContext, Map<Integer, RequestParamMetadata> parameterMetadataMap) {
        // 1. List 파라미터가 있는 지 확인
        boolean hasListParameter = parameterMetadataMap.values().stream().anyMatch(RequestParamMetadata::isListType);
        if ( hasListParameter ) {
            throw new RestClientCommonException(String.format("[%s] RestClient 는 List 타입의 파라미터를 지원하지 않습니다.", errorContext));
        }

        // 2. Request Body 수 확인
        long requestBodyCount = parameterMetadataMap.values().stream().filter(p -> {
            boolean isRequestHeader = p.isAnnotationRequestHeader();
            boolean isPathVariable = p.isAnnotationPathVariable();
            boolean canRequestParam = p.canRequestParam(this.isOnlyRequestParam(), this.emptyAllParameterAnnotation, this.pathValueNames);
            boolean isRestCallback = p.isRestCallback();
            return !isRequestHeader && !isPathVariable && !canRequestParam && !isRestCallback;
        }).count();

        // 3. GET, DELETE 인데 requestBody 로 판단되는 파라미터가 1개 이상이라면..
        if ( !this.isCanHasRequestBodyAnnotation() && requestBodyCount > 0 ) {
            StringBuilder errMessage = new StringBuilder(String.format("[%s] RequestBody 를 가질 수 없는 Method 입니다.", errorContext));
            if ( this.isHasPathValue() && !this.pathValueNames.isEmpty() ) {
                errMessage.append("\n");
                errMessage.append(" - PathVariable 을 Url 에 선언한 경우, @PathVariable 어노테이션은 필수 입니다.");
            }
            if ( this.hasRequestParamAnnotation ) {
                errMessage.append("\n");
                errMessage.append(" - RequestParam 어노테이션을 선언한 경우, Get Method 혹은 Delete 메소드는 선언되어 있지 않은 파라미터에 대해서 RequestBody 로 취급합니다.");
            }
            throw new RestClientCommonException(errMessage.toString());
        }

        // 4. POST, PUT, DELETE 인데 RequestBody 로 판단되는 파라미터가 1개가 아니라면..
        if ( this.isCanHasRequestBodyAnnotation() && ( requestBodyCount != 1 )) {
                throw new RestClientCommonException(String.format("[%s] POST, PUT, PATCH RequestMethod 는 MediaType 이 application/json 일 경우, RequestBody 1개는 필수 이여야 합니다. RequestBody 수 %d", errorContext, requestBodyCount));
        }

        // 5. 리턴 파입 기본 생성자 추출
        Class<?> returnRawType = this.returnMetadata.getRawType();
        if ( !returnRawType.isInterface() && !this.returnMetadata.isVoid() && !ClassUtil.isPrimitiveOrString(returnRawType) ) {
            try {
                returnRawType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RestClientCommonException(String.format("[%s] 리턴 타입 %s 는 기본 생성자가 있어야 ResponseMapper 로 변환 가능 합니다.", errorContext, returnRawType.getSimpleName()));
            }
        }

        // 6. RestResponse 와 RestCallback 은 같이 사용 X
        if (this.returnMetadata.isWrapRestResponse() && !this.restCallbackParameterList.isEmpty()) {
            throw new RestClientCommonException(String.format("[%s] RestClient 는 Return RestResponse 와 Argument RestCallback 의 병합 사용을 지원하지 않습니다.", errorContext));
        }
    }

}
