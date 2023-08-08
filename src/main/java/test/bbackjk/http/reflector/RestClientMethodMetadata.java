package test.bbackjk.http.reflector;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.MethodInvoker;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.interfaces.RestCallback;
import test.bbackjk.http.util.ClassUtil;
import test.bbackjk.http.util.ReflectorUtils;
import test.bbackjk.http.util.RestMapUtils;
import test.bbackjk.http.wrapper.RequestMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RestClientMethodMetadata {
    private static final List<Class<? extends Annotation>> ALLOWED_REQUEST_MAPPING_ANNOTATIONS = Stream.of(RequestMapping.class, GetMapping.class, PostMapping.class, PatchMapping.class, PutMapping.class, DeleteMapping.class).collect(Collectors.toUnmodifiableList());
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[a-z|0-9]+}");
    private static final LogHelper LOGGER = LogHelper.of(RestClientMethodMetadata.class);

    private final Annotation annotation;
    private final List<RestClientParamMetadata> parameterMetadataList;
    private final List<RestClientParamMetadata> restCallbackParameterList;
    private final RestClientReturnMetadata returnMetadata;
    @Getter
    private final RequestMethod requestMethod;
    private final String requestUrl;
    private final MediaType contentType;
    private final MediaType accept;
    private final List<String> pathValueNames;
    private final Map<Integer, RestClientParamMetadata> headerParameterFrame;
    private final Map<Integer, RestClientParamMetadata> pathParameterFrame;
    private final Map<Integer, RestClientParamMetadata> queryParameterFrame;
    private final Map<String, String> headerValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> pathValuesMap = new ConcurrentHashMap<>();
    private final Map<String, String> queryValuesMap = new ConcurrentHashMap<>();

    public RestClientMethodMetadata(Class<?> restClientInterface, Method method, String origin) {
        this.annotation = this.parseAnnotation(method);
        this.parameterMetadataList = this.getParamMetadataList(method.getParameters()).stream().collect(Collectors.toUnmodifiableList());
        this.restCallbackParameterList = this.parameterMetadataList.stream().filter(RestClientParamMetadata::isRestCallback).collect(Collectors.toUnmodifiableList());
        this.requestMethod = this.parseRequestMethodByAnnotation(this.annotation);
        String pathname = this.parsePathNameByAnnotation(this.annotation);
        this.requestUrl = this.getRequestUrl(origin, pathname);
        this.contentType = this.parseContentTypeByAnnotation(this.annotation);
        this.accept = this.parseAcceptByAnnotation(this.annotation);
        this.pathValueNames = this.getPathVariableNames(pathname);
        this.returnMetadata = new RestClientReturnMetadata(method);

        this.headerParameterFrame = RestMapUtils.toReadonly(this.getHeaderFrame(this.parameterMetadataList));
        this.pathParameterFrame = RestMapUtils.toReadonly(this.getPathFrame(this.parameterMetadataList));
        this.queryParameterFrame = RestMapUtils.toReadonly(this.getQueryFrame(this.parameterMetadataList));

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
        return this.parameterMetadataList.size();
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    public RequestMetadata applyArgs(Object[] args, LogHelper restClientLogger) {
        if ( args == null || args.length == 0 ) {
            return RequestMetadata.ofEmpty(this.requestUrl, this.contentType, restClientLogger);
        }

        Object bodyData = null;
        int argCount = args.length;
        int requestBodyCount = 0;
        for (int i=0; i<argCount; i++) {
            Object arg = args[i];

            if ( arg == null ) {
                if (
                        this.headerParameterFrame.containsKey(i) ||
                        this.pathParameterFrame.containsKey(i) ||
                        this.queryParameterFrame.containsKey(i)
                ) {
                    throw new RestClientCallException(" @RequestHeader 값은 Null 이 될 수 없습니다. ");
                }
                requestBodyCount++;
            } else {
                Class<?> argClass = arg.getClass();

                if ( this.headerParameterFrame.containsKey(i) ) {
                    this.headerValuesMap.put(this.headerParameterFrame.get(i).getParamName(), String.valueOf(arg));
                } else if ( this.pathParameterFrame.containsKey(i) ) {
                    this.pathValuesMap.put( this.pathParameterFrame.get(i).getParamName(), String.valueOf(arg));
                } else if ( this.queryParameterFrame.containsKey(i) ) {
                    if ( arg instanceof List ) {
                        throw new RestClientCallException(String.format("RestClient 는 List 타입의 파라미터를 지원하지 않습니다. 값 : %s", arg));
                    }
                    if ( arg instanceof Map ) {
                        Map<?, ?> map = (Map<?, ?>) arg;
                        map.forEach((k, v) -> this.queryValuesMap.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
                    } else if (!ClassUtil.isPrimitiveInString(arg.getClass())) {
                        MethodInvoker mi = new MethodInvoker();
                        mi.setTargetObject(arg);

                        Field[] fields = argClass.getDeclaredFields();
                        int fieldCount = fields.length;
                        for (int j=0; j<fieldCount;j++) {
                            Field field = fields[j];
                            String fieldName = field.getName();
                            mi.setTargetMethod(ClassUtil.getGetterMethodByField(field));
                            try {
                                mi.prepare();
                                Object value = mi.invoke();
                                this.queryValuesMap.put(fieldName, value == null ? null : String.valueOf(value));
                            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                     IllegalAccessException e) {
                                throw new RestClientCallException(String.format("해당 필드에 대한 Getter 메소드가 존재하지 않습니다. %s", fieldName));
                            }
                        }
                    } else {
                        this.queryValuesMap.put(this.queryParameterFrame.get(i).getParamName(), String.valueOf(arg));
                    }
                } else {
                    boolean isArgRestCallback = arg instanceof RestCallback;
                    if ( isArgRestCallback ) {
                        continue;
                    }

                    requestBodyCount++;

                    if ( this.isFormContent() && this.isCanHasRequestBodyAnnotation() ) {
                        if ( argClass.getAnnotations().length == 0 ) {
                            bodyData = arg;
                        } else {
                            throw new RestClientCallException("ContentType 이 x-www-urlencoded 인 경우, FormData 로 보낼 파라미터에 Annotation 이 존재해서는 안됩니다.");
                        }
                    } else {
                        RequestBody requestBody = argClass.getAnnotation(RequestBody.class);
                        if ( requestBody != null ) {
                            throw new RestClientCallException("ContentType 이 x-www-urlencoded 이 아닌 경우, BodyData 로 보낼 파라미터에 @RequestBody 어노테이션은 필수입니다.");
                        }
                        bodyData = arg;
                    }
                }
            }
        }

        if ( requestBodyCount > 0 ) {
            // log body data...
        }

        return RequestMetadata.of(this.requestUrl, this.contentType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger);
    }

    public boolean isWrap() {
        return this.returnMetadata.isWrap();
    }

    public boolean isMap() {
        return this.returnMetadata.isWrapMap();
    }

    public boolean isWrapList() {
        return isWrapList(null);
    }

    public boolean isString() {
        return this.returnMetadata.isString();
    }

    public boolean isVoid() {
        return this.returnMetadata.isVoid();
    }

    public boolean isWrapOptional() {
        return this.returnMetadata.isWrapOptional();
    }

    public Class<?> getSecondRawType() {
        return this.returnMetadata.getSecondRawType();
    }

    public Class<?> getRawType() {
        return this.returnMetadata.getRawType();
    }

    public boolean isWrapList(Class<?> clazz) {
        return this.returnMetadata.isWrapList(clazz);
    }

    public boolean isWrapRestResponse() {
        return this.returnMetadata.isWrapRestResponse();
    }

    private List<String> getPathVariableNames(String pathname) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pathname);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String values = matcher.group(); // "{values}"
            result.add(values.substring(1, values.length()-1)); // "values"
        }
        return result;
    }
    
    private List<RestClientParamMetadata> getParamMetadataList(Parameter[] parameters) {
        if ( parameters == null ) {
            return Collections.emptyList();
        }
        List<RestClientParamMetadata> result = new ArrayList<>();
        int paramCount = parameters.length;
        for (int i=0; i<paramCount;i++) {
            result.add(new RestClientParamMetadata(parameters[i]));
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
            if ( copyPathname.charAt(0) == '/') {
                return copyOrigin + copyPathname.substring(1);
            } else {
                return copyOrigin + copyPathname;
            }
        } else {
            if ( copyPathname.charAt(0) == '/') {
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

    private Map<Integer, RestClientParamMetadata> getHeaderFrame(List<RestClientParamMetadata> list) {
        Map<Integer, RestClientParamMetadata> result = new LinkedHashMap<>();
        int paramCount = list.size();
        for (int i=0; i<paramCount; i++) {
            RestClientParamMetadata param = list.get(i);
            if (param.isRequestHeaderAnnotation()) {
                result.put(i, param);
            }
        }
        return result;
    }

    private Map<Integer, RestClientParamMetadata> getPathFrame(List<RestClientParamMetadata> list) {
        Map<Integer, RestClientParamMetadata> result = new LinkedHashMap<>();
        int paramCount = list.size();
        for (int i=0; i<paramCount; i++) {
            RestClientParamMetadata param = list.get(i);
            if (param.isPathVariableAnnotation()) {
                result.put(i, param);
            }
        }
        return result;
    }

    private Map<Integer, RestClientParamMetadata> getQueryFrame(List<RestClientParamMetadata> list) {
        boolean hasRequestParamAnnotation = list.stream().anyMatch(RestClientParamMetadata::isRequestParamAnnotation);
        boolean isEmptyAllAnnotation = list.stream().noneMatch(RestClientParamMetadata::hasAnnotation);
        if ( (this.isFormContent() && this.isCanHasRequestBodyAnnotation()) || hasRequestParamAnnotation ) {
            return this.onlyRequestParamHandler(list);
        } else if ( isEmptyAllAnnotation ) {
            return this.emptyAnnotationAllParameterHandler(list);
        } else {
            return this.emptyRequestParamHandler(list);
        }
    }

    private Map<Integer, RestClientParamMetadata> onlyRequestParamHandler(List<RestClientParamMetadata> list) {
        Map<Integer, RestClientParamMetadata> result = new LinkedHashMap<>();
        int paramCount = list.size();
        for (int i=0; i<paramCount; i++) {
            RestClientParamMetadata param = list.get(i);
            if (param.isRequestParamAnnotation()) {
                result.put(i, param);
            }
        }
        return result;
    }

    private Map<Integer, RestClientParamMetadata> emptyAnnotationAllParameterHandler(List<RestClientParamMetadata> list) {
        Map<Integer, RestClientParamMetadata> result = new LinkedHashMap<>();
        int paramCount = list.size();
        for (int i=0; i<paramCount; i++) {
            RestClientParamMetadata param = list.get(i);
            if (!this.pathValueNames.contains(param.getParamName())) {
                result.put(i, param);
            }
        }
        return result;
    }

    private Map<Integer, RestClientParamMetadata> emptyRequestParamHandler(List<RestClientParamMetadata> list) {
        Map<Integer, RestClientParamMetadata> result = new LinkedHashMap<>();
        int paramCount = list.size();
        for (int i=0; i<paramCount; i++) {
            RestClientParamMetadata param = list.get(i);
            if (
                    !param.hasAnnotation() &&                                   // Parameter 가 어노테이션이 없고,
                            !this.pathValueNames.contains(param.getParamName()) &&      // PathValue 에 포함되어있지 않으며,
                            !param.isRestCallback()                                     // RestCallback 이 아닌 경우
            ) {
                result.put(i, param);
            }
        }
        return result;
    }

    /**
     * TODO: 추가
     * try {
     *             clazz.getConstructor();
     *         } catch (NoSuchMethodException e) {
     *             throw new RestClientDataMappingException(String.format("%s 클래스의 기본 생성자는 필수입니다.", clazz.getSimpleName()));
     *         }
     */
    protected void valid(Class<?> restClientInterface, Method m) {
        String errorContextFormat = String.format("%s#%s", restClientInterface.getSimpleName(), m.getName());
        this.validRequestBodyCount(errorContextFormat);
        this.validParameter(errorContextFormat);
    }

    protected void validRequestBodyCount(String errContextMessage) {
        boolean hasRequestParamAnnotation = parameterMetadataList.stream().anyMatch(RestClientParamMetadata::isRequestParamAnnotation);
        int totalParamCount = this.getParamCount();

        int headerValueCount = this.headerParameterFrame.size();
        int pathValueCount = this.pathParameterFrame.size();
        int queryValueCount = this.queryParameterFrame.size();
        int restCallbackCount = this.restCallbackParameterList.size();

        int foundParamCount = headerValueCount + pathValueCount + queryValueCount + restCallbackCount;
        int requestBodyCount = totalParamCount - foundParamCount;

        if ( this.isCanHasRequestBodyAnnotation() ) {
            if ( requestBodyCount != 1 ) {
                throw new RestClientCallException(String.format("[%s] POST, PUT, PATCH RequestMethod 는 MediaType 이 application/json 일 경우, RequestBody 1개는 필수 이여야 합니다. RequestBody 수 %d", errContextMessage, requestBodyCount));
            }
        } else {
            if ( requestBodyCount > 0 ) {
                StringBuilder errMessage = new StringBuilder(String.format("[%s] RequestBody 를 가질 수 없는 Method 입니다.", errContextMessage));
                if ( this.isHasPathValue() &&  this.pathValueNames.size() != pathValueCount ) {
                    errMessage.append("\n");
                    errMessage.append(" - PathVariable 을 Url 에 선언한 경우, @PathVariable 어노테이션은 필수 입니다.");
                }
                if ( hasRequestParamAnnotation ) {
                    errMessage.append("\n");
                    errMessage.append(" - RequestParam 어노테이션을 선언한 경우, Get Method 혹은 Delete 메소드는 선언되어 있지 않은 파라미터에 대해서 RequestBody 로 취급합니다.");
                }
                throw new RestClientCallException(errMessage.toString());
            }
        }
    }
    protected void validParameter(String errContextMessage) {
        boolean hasListParameter = this.parameterMetadataList.stream().anyMatch(RestClientParamMetadata::isList);
        if ( hasListParameter ) {
            throw new RestClientCallException(String.format("[%s] RestClient 는 List 타입의 파라미터를 지원하지 않습니다.", errContextMessage));
        }

        if ( this.queryParameterFrame.size() > 0 ) {
            boolean hasNoGetterParameter = this.parameterMetadataList.stream()
                    .filter(RestClientParamMetadata::isReference)
                    .anyMatch(paramMetadata -> !paramMetadata.hasAllGetterField());
            if ( hasNoGetterParameter ) {
                throw new RestClientCallException(String.format("[%s] RestClient 에서 Reference 타입의 파라미터는 Getter 메소드는 필수입니다.", errContextMessage));
            }
        }
    }
}
