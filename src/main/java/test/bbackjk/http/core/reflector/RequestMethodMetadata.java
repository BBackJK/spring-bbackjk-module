package test.bbackjk.http.core.reflector;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.MethodInvoker;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.exceptions.RestClientCommonException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.util.ClassUtil;
import test.bbackjk.http.core.util.ReflectorUtils;
import test.bbackjk.http.core.util.RestMapUtils;
import test.bbackjk.http.core.wrapper.RequestMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestMethodMetadata {
    private static final List<Class<? extends Annotation>> ALLOWED_REQUEST_MAPPING_ANNOTATIONS = Stream.of(RequestMapping.class, GetMapping.class, PostMapping.class, PatchMapping.class, PutMapping.class, DeleteMapping.class).collect(Collectors.toUnmodifiableList());
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[a-z|0-9]+}");
    private static final LogHelper LOGGER = LogHelper.of(RequestMethodMetadata.class);

    private final Annotation annotation;
    private final Map<Integer, RequestParamMetadata> parameterMetadataMap;
    private final List<RequestParamMetadata> restCallbackParameterList;
    private final boolean hasRequestParamAnnotation;
    private final boolean emptyAllParameterAnnotation;
    private final RequestReturnMetadata returnMetadata;
    @Getter
    private final RequestMethod requestMethod;
    private final String requestUrl;
    private final MediaType contentType;
    private final MediaType accept;
    private final List<String> pathValueNames;

    private final Map<String, String> headerValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> pathValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> queryValuesMap = new ConcurrentHashMap<>();

    public RequestMethodMetadata(Class<?> restClientInterface, Method method, String origin) {
        this.annotation = this.parseAnnotation(method);
        this.parameterMetadataMap = RestMapUtils.toReadonly(this.getParamMetadataList(method.getParameters()));
        this.hasRequestParamAnnotation = this.parameterMetadataMap.values().stream().anyMatch(RequestParamMetadata::isAnnotationRequestParam);
        this.emptyAllParameterAnnotation = this.parameterMetadataMap.values().stream().noneMatch(RequestParamMetadata::hasAnnotation);
        this.restCallbackParameterList = this.parameterMetadataMap.values().stream().filter(RequestParamMetadata::isRestCallback).collect(Collectors.toUnmodifiableList());
        this.requestMethod = this.parseRequestMethodByAnnotation(this.annotation);
        String pathname = this.parsePathNameByAnnotation(this.annotation);
        this.requestUrl = this.getRequestUrl(origin, pathname);
        this.contentType = this.parseContentTypeByAnnotation(this.annotation);
        this.accept = this.parseAcceptByAnnotation(this.annotation);
        this.pathValueNames = this.getPathVariableNames(pathname);
        this.returnMetadata = new RequestReturnMetadata(method);

        this.valid(restClientInterface, method);
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

    public boolean isXmlAccept() {
        return MediaType.APPLICATION_XML.equalsTypeAndSubtype(this.accept);
    }

    public boolean isJsonContent() {
        return MediaType.APPLICATION_JSON.equalsTypeAndSubtype(this.contentType);
    }

    public boolean isHasPathValue() {
        return PATH_VARIABLE_PATTERN.matcher(this.requestUrl).find();
    }

    public int getParamCount() {
        return this.parameterMetadataMap.size();
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    // TODO : 리팩토링 (RequestMetaValueHandler 객체 추가하여 소스코드 정리.)
    public RequestMetadata applyArgs(Object[] args, LogHelper restClientLogger) {
        if ( args == null || args.length == 0 ) {
            return RequestMetadata.ofEmpty(this.requestUrl, this.contentType, restClientLogger);
        }

        boolean isOnlyRequestParam = (this.isFormContent() && this.isCanHasRequestBodyAnnotation()) || this.hasRequestParamAnnotation;

        Object bodyData = null;
        int argCount = args.length;
        List<Object> requestBodyList = new ArrayList<>();
        for (int i=0; i<argCount; i++) {
            Optional<Object> arg = Optional.ofNullable(args[i]);
            RequestParamMetadata parameter = Optional.ofNullable(this.parameterMetadataMap.get(i)).orElseThrow(
                    () -> new RestClientCallException(" Parameter 가 존재하지 않습니다. ")
            );

            boolean isRequestHeader = parameter.isAnnotationRequestHeader();
            boolean isPathVariable = parameter.isAnnotationPathVariable();
            boolean canRequestParam = parameter.canRequestParam(isOnlyRequestParam, this.emptyAllParameterAnnotation, this.pathValueNames);

            if ( isRequestHeader ) {
                arg.ifPresent(o -> this.headerValuesMap.put(parameter.getParamName(), String.valueOf(o)));
            } else if ( isPathVariable ) {
                arg.ifPresent(o -> this.pathValuesMap.put(parameter.getParamName(), String.valueOf(o)));
            } else if ( canRequestParam ) {
                arg.ifPresent(o -> {
                    boolean isReturnMap = parameter.isMapType();
                    boolean isDetailList = arg.orElse(null) instanceof List;
                    boolean isDetailMap = arg.orElse(null) instanceof Map;
                    if ( isDetailList ) throw new RestClientCallException("RestClient 는 List 타입의 파라미터는 지원하지 않습니다.");

                    if ( isReturnMap || isDetailMap ) {
                        Map<?, ?> map = (Map<?, ?>) o;
                        map.forEach((k, v) -> this.queryValuesMap.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
                    } else if ( parameter.isReferenceType() ) {
                        MethodInvoker mi = new MethodInvoker();
                        mi.setTargetObject(o);
                        List<String> getterMethods = parameter.getGetterMethodNames();
                        for ( String fieldName : getterMethods ) {
                            if ( fieldName != null ) {
                                try {
                                    mi.setTargetMethod(ClassUtil.getGetterMethodByFieldName(fieldName));
                                    mi.prepare();
                                    Object v = mi.invoke();
                                    if ( v != null ) {
                                        this.queryValuesMap.put(fieldName, String.valueOf(v));
                                    }
                                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                         IllegalAccessException e) {
                                    // ignore..
                                }
                            }
                        }
                    } else {
                        this.queryValuesMap.put(parameter.getParamName(), String.valueOf(o));
                    }
                });
            } else {
                boolean isRestCallback = parameter.isRestCallback();
                boolean isDetailRestCallback = arg.orElse(null) instanceof Map;
                if ( isRestCallback || isDetailRestCallback ) {
                    continue;
                }

                requestBodyList.add(arg.orElse(null));

                if ( this.isFormContent() && this.isCanHasRequestBodyAnnotation() ) {
                    if ( parameter.hasAnnotation() ) {
                        throw new RestClientCallException("ContentType 이 x-www-urlencoded 인 경우, FormData 로 보낼 파라미터에 Annotation 이 존재해서는 안됩니다.");
                    }
                } else {
                    if (!parameter.isAnnotationRequestBody()) {
                        throw new RestClientCallException("ContentType 이 x-www-urlencoded 이 아닌 경우, BodyData 로 보낼 파라미터에 @RequestBody 어노테이션은 필수입니다.");
                    }
                }
                if ( arg.isPresent() ) bodyData = arg.get();
            }
        }

        if ( requestBodyList.size() > 1 ) {
            LOGGER.warn("Request Body 로 인식되는 파라미터가 1개 이상입니다.");
            LOGGER.warn("Request Body : {}", requestBodyList);
        }

        return RequestMetadata.of(this.requestUrl, this.contentType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger);
    }

    public boolean isReturnWrap() {
        return this.returnMetadata.isWrap();
    }

    public boolean isReturnMap() {
        return this.returnMetadata.isWrapMap();
    }

    public boolean isReturnList() {
        return this.returnMetadata.isWrapList();
    }
    public boolean isReturnList(Class<?> clazz) {
        return this.returnMetadata.isWrapList(clazz);
    }

    public boolean isReturnString() {
        return this.returnMetadata.isString();
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
                if ( copyOrigin.isBlank() && copyPathname.isBlank() ) {
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

    @NotNull
    private MediaType parseAcceptByAnnotation(@Nullable Annotation annotation) {
        MediaType defaultContentType = MediaType.APPLICATION_JSON;
        if ( annotation == null ) {
            return defaultContentType;
        }
        String[] accepts = (String[]) ReflectorUtils.annotationMethodInvoke(annotation, "produces");
        if ( accepts == null || accepts.length < 1 ) {
            return defaultContentType;
        }

        String firstAccept = accepts[0];
        String[] acceptSplit = firstAccept.split("/");
        try {
            return new MediaType(acceptSplit[0], acceptSplit[1]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LOGGER.warn("Annotation 으로부터 accept 을 파싱하다 실패하였습니다. 원인 :: {}", e.getMessage());
            return defaultContentType;
        }
    }

    protected void valid(Class<?> restClientInterface, Method m) {
        String errorContextFormat = String.format("%s#%s", restClientInterface.getSimpleName(), m.getName());
        // List 파라미터가 있는 지 확인
        boolean hasListParameter = this.parameterMetadataMap.values().stream().anyMatch(RequestParamMetadata::isListType);
        if ( hasListParameter ) {
            throw new RestClientCommonException(String.format("[%s] RestClient 는 List 타입의 파라미터를 지원하지 않습니다.", errorContextFormat));
        }

        // Request Body 수 확인
        boolean isOnlyRequestParam = (this.isFormContent() && this.isCanHasRequestBodyAnnotation()) || this.hasRequestParamAnnotation;

        long requestBodyCount = this.parameterMetadataMap.values().stream().filter(p -> {
            boolean isRequestHeader = p.isAnnotationRequestHeader();
            boolean isPathVariable = p.isAnnotationPathVariable();
            boolean canRequestParam = p.canRequestParam(isOnlyRequestParam, this.emptyAllParameterAnnotation, this.pathValueNames);
            boolean isRestCallback = p.isRestCallback();
            return !isRequestHeader && !isPathVariable && !canRequestParam && !isRestCallback;
        }).count();

        // GET, DELETE 인데 requestBody 로 판단되는 파라미터가 1개 이상이라면..
        if ( !this.isCanHasRequestBodyAnnotation() && requestBodyCount > 0 ) {
            StringBuilder errMessage = new StringBuilder(String.format("[%s] RequestBody 를 가질 수 없는 Method 입니다.", errorContextFormat));
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

        // POST, PUT, DELETE 인데 RequestBody 로 판단되는 파라미터가 1개가 아니라면..
        if ( this.isCanHasRequestBodyAnnotation() && ( requestBodyCount != 1 )) {
                throw new RestClientCommonException(String.format("[%s] POST, PUT, PATCH RequestMethod 는 MediaType 이 application/json 일 경우, RequestBody 1개는 필수 이여야 합니다. RequestBody 수 %d", errorContextFormat, requestBodyCount));
        }

        // 리턴 파입 기본 생성자 추출
        Class<?> returnRawType = this.returnMetadata.getRawType();
        if ( !returnRawType.isInterface() && !this.returnMetadata.isVoid() && !ClassUtil.isPrimitiveInString(returnRawType) ) {
            try {
                returnRawType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RestClientCommonException(String.format("[%s] 리턴 타입 %s 는 기본 생성자가 있어야 ResponseMapper 로 변환 가능 합니다.", errorContextFormat, returnRawType.getSimpleName()));
            }
        }
    }

}
