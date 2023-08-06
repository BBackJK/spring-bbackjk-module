package test.bbackjk.http.reflector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.MethodInvoker;
import org.springframework.web.bind.annotation.*;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.helper.RestCallback;
import test.bbackjk.http.util.ClassUtil;
import test.bbackjk.http.util.ReflectorUtils;
import test.bbackjk.http.wrapper.RequestMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RequestMethodMetadata {
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[a-z|0-9]+}");
    private final Class<?> restClientInterface;
    private final boolean hasRequestParamAnnotation;
    private final boolean canHasRequestBody;
    private final boolean isFormContent;
    private final boolean hasPathVariable;
    private final List<Parameter> restCallbackList;
    private final List<String> pathVariableNames;
    private final Map<Integer, String> headerValuesFrame;
    private final Map<Integer, String> pathValuesFrame;
    private final Map<Integer, String> queryValuesFrame;
    private final LogHelper restClientLogger;
    private final LogHelper logger = LogHelper.of(getClass());
    
    public RequestMethodMetadata(RestClientMethodInvoker m, LogHelper restClientLogger, Class<?> restClientInterface) {
        this.restClientInterface = restClientInterface;
        Method method = m.getMethod();
        this.hasRequestParamAnnotation = this.hasRequestParamByMethod(method.getParameterAnnotations());
        this.canHasRequestBody = this.checkHasRequestBody(m.getRequestMethod());
        this.isFormContent = m.isFormContent();
        this.hasPathVariable = this.checkHasPathVariable(m.getRequestPathname());
        this.pathVariableNames = this.getPathVariableNames(m.getRequestPathname());

        this.restCallbackList = this.getRestCallbackList(method.getParameters());
        this.headerValuesFrame = this.getHeaderValuesFrame(method.getParameters());
        this.pathValuesFrame = this.getPathValuesFrame(method.getParameters());
        this.queryValuesFrame = this.getQueryValuesFrame(method);
        this.restClientLogger = restClientLogger;

        this.validationMethodParameters(method);
    }

    public RequestMetadata applyArgs(String origin, String pathname, MediaType mediaType, Object[] args) {
        if ( args == null || args.length == 0 ) {
            return RequestMetadata.ofEmpty(origin, pathname, mediaType, this.restClientLogger);
        }

        Map<String, String> headerValuesMap = new LinkedHashMap<>();
        Map<String, String> pathValuesMap = new LinkedHashMap<>();
        Map<String, String> queryValuesMap = new LinkedHashMap<>();
        Object bodyData = null;

        int argsCount = args.length;
        int bodyDataCount = 0;
        for (int i=0; i<argsCount; i++) {
            Object arg = args[i];
            if ( arg == null ) {
                if (this.headerValuesFrame.containsKey(i)) {
                    throw new RestClientCallException(" @RequestHeader 값은 Null 이 될 수 없습니다. ");
                } else if (this.pathValuesFrame.containsKey(i)) {
                    throw new RestClientCallException(" @PathVariable 값은 Null 이 될 수 없습니다. ");
                } else if (this.queryValuesFrame.containsKey(i)) {
                    queryValuesMap.put(this.pathValuesFrame.get(i), null);
                } else {
                    bodyDataCount++;
                }
            } else {
                if (this.headerValuesFrame.containsKey(i)) {
                    headerValuesMap.put(this.headerValuesFrame.get(i), String.valueOf(arg));
                } else if (this.pathValuesFrame.containsKey(i)) {
                    pathValuesMap.put(this.pathValuesFrame.get(i), String.valueOf(arg));
                } else if (this.queryValuesFrame.containsKey(i)) {
                    // List 형태로는 올 수가 없다.
                    if ( arg instanceof Collection ) {
                        throw new RestClientCallException(" 파라미터는 List 가 올 수 없습니다. ");
                    }

                    // query value 로는 Object or Map 형태가 올 수 있다.
                    Class<?> argClazz = arg.getClass();
                    if ( arg instanceof Map ) {
                        Map<?, ?> map = (Map<?, ?>) arg;
                        map.forEach((k, v) -> queryValuesMap.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
                    } else if ( !ClassUtil.isPrimitiveInString(argClazz) ) {
                        Field[] fields = argClazz.getDeclaredFields();
                        MethodInvoker mi = new MethodInvoker();
                        mi.setTargetObject(arg);
                        int fieldsCount = fields.length;
                        for (int j = 0; j < fieldsCount; j++) {
                            Field field = fields[j];
                            String fieldName = field.getName();
                            mi.setTargetMethod(ClassUtil.getGetterMethodByField(field));
                            try {
                                mi.prepare();
                                Object value = mi.invoke();
                                queryValuesMap.put(fieldName, value == null ? null : String.valueOf(value));
                            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                     IllegalAccessException e) {
                                this.restClientLogger.err(e.getMessage());
                                throw new RestClientCallException(String.format("해당 필드에 대한 Getter 메소드가 존재하지 않습니다. %s", fieldName));
                            }
                        }
                    } else { // 일반 primitive, String 인 경우
                        queryValuesMap.put(this.queryValuesFrame.get(i), String.valueOf(arg));
                    }
                } else if (arg instanceof RestCallback) {
                    // ignore
                } else {
                    bodyDataCount++;
                    if ( bodyDataCount > 1 ) {
                        throw new RestClientCallException(String.format("@RequestBody 어노테이션은 2개 이상 있을 수 없습니다. 첫번째 argument :: %s, 두번째 argument :: %s", bodyData, arg));
                    }
                    Class<?> argClazz = arg.getClass();

                    if ( this.isFormContent && this.canHasRequestBody ) {
                        if ( argClazz.getAnnotations().length == 0 ) {
                            bodyData = arg;
                        } else {
                            throw new RestClientCallException("ContentType 이 x-www-urlencoded 인 경우, FormData 로 보낼 파라미터에 Annotation 이 존재해서는 안됩니다.");
                        }
                    } else {
                        RequestBody requestBody = argClazz.getAnnotation(RequestBody.class);
                        if ( requestBody != null ) {
                            throw new RestClientCallException("ContentType 이 x-www-urlencoded 이 아닌 경우, BodyData 로 보낼 파라미터에 @RequestBody 어노테이션은 필수입니다.");
                        }
                        bodyData = arg;
                    }
                }
            }
        }

        return RequestMetadata.of(
                origin, pathname, mediaType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger
        );
    }

    public boolean hasRestCallback() {
        return !this.restCallbackList.isEmpty();
    }

    /**
     * RequestBody 를 가질 수 있는지 여부 판단.
     * ISSUE 1 > GET, DELETE 는 가질 수 없다고 판단함.
     */
    private boolean checkHasRequestBody(RequestMethod requestMethod) {
        return requestMethod == RequestMethod.POST ||
                requestMethod == RequestMethod.PUT ||
                requestMethod == RequestMethod.PATCH;
    }

    /**
     * pathname 에 {value} 같은 format 이 포함 유무 여부 판단
     */
    private boolean checkHasPathVariable(String pathname) {
        return pathname != null && PATH_VARIABLE_PATTERN.matcher(pathname).find();
    }

    /**
     * 파라미터의 Annotation 중 RequestParam 어노테이션을 가지고 있는지 판단.
     */
    private boolean hasRequestParamByMethod(Annotation[][] paramAnnotations) {
        boolean hasRequestParam = false;
        if ( paramAnnotations == null ) {
            return false;
        }
        int paramAnnotationCount = paramAnnotations.length;
        for (int i=0; i< paramAnnotationCount; i++) {
            for (Annotation paramAnnotation : paramAnnotations[i]) {
                if (paramAnnotation.annotationType() == RequestParam.class) {
                    hasRequestParam = true;
                    break;
                }
            }
        }
        return hasRequestParam;
    }

    /**
     * pathname 에 {value} 같은 format 을 찾아 List 로 Add
     */
    private List<String> getPathVariableNames(String pathname) {
        if ( !this.hasPathVariable ) {
            return Collections.emptyList();
        }
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pathname);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String values = matcher.group(); // "{values}"
            result.add(values.substring(1, values.length()-1)); // "values"
        }
        return result;
    }

    private List<Parameter> getRestCallbackList(Parameter[] parameters) {
        List<Parameter> result = new ArrayList<>();
        int paramCount = parameters.length;
        for (int i=0; i<paramCount; i++) {
            Parameter param = parameters[i];
            if ( param.getAnnotations().length == 0 && RestCallback.class.equals(param.getType())) {
                result.add(param);
            }
        }
        return result;
    }

    /**
     * method 의 파라미터들에 대해서 RequestHeader 어노테이션을 가진 파라미터에 대해 Map 으로 설정
     * Key: 파라미터 인덱스 순서
     * Value: 파라미터명 혹은 어노테이션에 기재된 value, name 값
     */
    private Map<Integer, String> getHeaderValuesFrame(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        int paramCount = parameters.length;
        for(int i=0; i<paramCount; i++) {
            Parameter param = parameters[i];
            RequestHeader requestHeader = param.getAnnotation(RequestHeader.class);
            if ( requestHeader == null ) continue;
            String annotationParamName = this.getParamNameByAnnotation(requestHeader);
            result.put(i, annotationParamName == null ? param.getName() : annotationParamName);
        }
        return result;
    }

    /**
     * PathVariable 값들을 판단하는 함수.
     * pathname 에 path-variable 이 없으면 값이 없다고 판단.
     * 있을 시, @PathVariable 어노테이션이 있거나 parameter 인자명 값이 pathVariableName 에 포함되어있는 값들로만 여부 판단.
     * Key: 파라미터 인덱스 순서
     * Value: 파라미터명 혹은 어노테이션에 기재된 value, name 값
     */
    private Map<Integer, String> getPathValuesFrame(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        if ( !this.hasPathVariable || parameters == null ) {
            return result;
        }

        int paramCount = parameters.length;
        for (int i=0; i<paramCount; i++) {
            Parameter param = parameters[i];
            PathVariable pathVariable = param.getAnnotation(PathVariable.class);
            if ( pathVariable == null ) continue;
            String paramName = param.getName();

            String annotationParamName = this.getParamNameByAnnotation(pathVariable);
            result.put(i, annotationParamName == null ? paramName : annotationParamName);
        }

        return result;
    }

    /**
     * Query Parameter 값들을 판단하는 함수
     * 1. RequestMappingAnnotation 이 POST, PUT, PATCH 이여서, RequestBody 를 가질 수 있을 때, MediaType 이 x-www-form-urlencoded 인 경우, @RequestBody 어노테이션을 사용하지 않는다,
     *      ==> @RequestParam 어노테이션이 있는 경우에만 query value 로 판단.
     * 2. 그 외 인 경우
     *  2-1. 모든 파라미터가 어노테이션을 가지고 있지 않은 경우
     *      ==> path value 에 포함되지 않은 값들만 query value 로 판단.
     *  2-2. 파라미터 중 @RequestParam 어노테이션을 가지고 있는 파라미터가 있을 경우
     *      ==> @RequestParam 어노테이션을 가진 값들만 query value 로 판단.
     *  2-3. 파라미터들 중 @RequestParam 어노테이션을 하나도 가지고 있지 않은 경우
     *      ==> 어노테이션을 가지고 있지 않은 값들에 대해서만 query value 로 판단.
     */
    private Map<Integer, String> getQueryValuesFrame(Method method) {
        Parameter[] parameters = method.getParameters();

        if ( this.isFormContent && this.canHasRequestBody ) {

            // * RequestMethod 가 POST, PATCH, PUT 이고, MediaType 이 x-www-form-urlencoded 인 경우
            // @RequestBody 가 없이 Request Body Data 를 사용하므로, 이 때는 @RequestParam 어노테이션이 있을 때 에만 Query Value 로 판단 한다.
            return this.requestFormBodyQueryValuesHandler(parameters);

        } else {
            if ( method.getParameterAnnotations().length == 0 ) {

                // * MediaType 이 x-www-form-urlencoded 이 아닌 경우
                // 1. 모든 파라미터가 어노테이션을 하나도 갖고 있지 않은 경우 - path value 에 포함하지 않은 값들 에 대해서만 포함시킴.
                return this.requestJsonBodyEmptyAllAnnotationHandler(parameters);

            } else if ( this.hasRequestParamAnnotation ) {

                // 2. 어노테이션 중 @RequestParam 어노테이션이 있는 경우 - RequestParam 어노테이션을 가지고 있는 인자만 추출
                return this.requestJsonBodyHasRequestParamHandler(parameters);

            } else {
                // 3. @RequestParam 이 없는 경우 - 어노테이션을 가지고 있지 않은 파라미터만 추출
                return this.requestJsonBodyEmptyRequestParamHandler(parameters);
            }
        }
    }


    /**
     * RequestMethod 가 POST, PATCH, PUT 이고, MediaType 이 x-www-form-urlencoded 인 경우
     * 1. @RequestBody 가 없이 Request Body Data 를 사용하므로, 이 때는 @RequestParam 어노테이션이 있을 때 에만 Query Value 로 판단 한다.
     */
    private Map<Integer, String> requestFormBodyQueryValuesHandler(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        int paramCount = parameters.length;
        for (int i=0;i<paramCount;i++) {
            Parameter param = parameters[i];
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            boolean hasRequestParam = requestParam != null;
            if ( !hasRequestParam || this.pathVariableNames.contains(param.getName()) ) continue;
            String paramName = this.getParamNameByAnnotation(requestParam);
            result.put(i, paramName == null ? param.getName() : paramName);
        }
        return result;
    }

    // * MediaType 이 x-www-form-urlencoded 이 아닌 경우
    // 1. 모든 파라미터가 어노테이션을 하나도 갖고 있지 않은 경우 - path value 에 포함하지 않은 값들 에 대해서만 포함시킴.
    private Map<Integer, String> requestJsonBodyEmptyAllAnnotationHandler(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        int paramCount = parameters.length;
        for (int i=0;i<paramCount;i++) {
            Parameter param = parameters[i];
            if ( !this.pathVariableNames.contains(param.getName()) ) {
                result.put(i, param.getName());
            }
        }
        return result;
    }

    // 2. 어노테이션 중 @RequestParam 어노테이션이 있는 경우 - RequestParam 어노테이션을 가지고 있는 인자만 query value 로 판단한다.
    private Map<Integer, String> requestJsonBodyHasRequestParamHandler(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        int paramCount = parameters.length;
        for (int i=0;i<paramCount;i++) {
            Parameter param = parameters[i];
            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            if ( requestParam == null ) continue;
            String paramName = this.getParamNameByAnnotation(requestParam);
            result.put(i, paramName == null ? param.getName() : paramName);
        }
        return result;
    }

    // 3. @RequestParam 이 없는 경우 - 어노테이션을 가지고 있지 않은 파라미터만 추출
    private Map<Integer, String> requestJsonBodyEmptyRequestParamHandler(Parameter[] parameters) {
        Map<Integer, String> result = new HashMap<>();
        int paramCount = parameters.length;
        for (int i=0;i<paramCount;i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            if (
                    param.getAnnotations().length == 0 &&
                    !this.pathVariableNames.contains(paramName) &&
                    !this.restCallbackList.contains(param)
            ) {
                result.put(i, paramName == null ? param.getName() : paramName);
            }
        }
        return result;
    }

    /**
     * 각 Mapping Annotation 마다 value, name 값들이 존재하는데, 이 값들의 유무 판단을 함수로 추출하여 공통으로 사용
     */
    @Nullable
    private String getParamNameByAnnotation(Annotation annotation) {
        String result = null;
        String value = (String) ReflectorUtils.annotationMethodInvoke(annotation, "value");
        String name = (String) ReflectorUtils.annotationMethodInvoke(annotation, "name");
        if ( name != null && !name.isBlank() ) {
            result = name;
        } else if ( value != null && !value.isBlank() ) {
            result = value;
        }
        return result;
    }

    /**
     * 설정 후 유효성 체크
     * 1. RequestBody 수 체크
     * 2. Parameter 중 타입이 List 인 것들을 확인.
     * 3. Parameter 중 Query Value 의 타입이 Primitive 와 String 이 아닌 경우 getter 는 필수.
     */
    private void validationMethodParameters(Method m) {
        if ( m == null || m.getParameters() == null ) {
            return;
        }
        // 1. RequestBody 수 체크
        this.checkRequestBodyCountValid(m);

        // 2. Parameter 중 타입이 List 인 것들을 확인.
        // 3. Parameter 중 Query Value 의 타입이 Primitive 와 String 이 아닌 경우 getter 는 필수.
        this.checkListParamValid(m);
    }

    private void checkRequestBodyCountValid(@NotNull Method m) {
        Parameter[] parameters = m.getParameters();
        int paramCount = parameters.length;
        int headerValueSize = this.headerValuesFrame.size();
        int pathValueSize = this.pathValuesFrame.size();
        int queryValueSize = this.queryValuesFrame.size();
        int restCallbackSize = this.restCallbackList.size();

        int totalFoundedParamCount = headerValueSize + pathValueSize + queryValueSize;

        int requestBodyCount = paramCount - totalFoundedParamCount - restCallbackSize;

        // RequestBody 를 가질 수 있으면.
        if ( this.canHasRequestBody ) {
            if ( requestBodyCount != 1 ) {
                throw new RestClientCallException(String.format("[%s#%s] POST, PUT, PATCH RequestMethod 는 MediaType 이 application/json 일 경우, RequestBody 1개는 필수 이여야 합니다. RequestBody 수 %d", this.restClientInterface.getSimpleName(), m.getName(), requestBodyCount));
            }
        } else {
            if ( requestBodyCount > 0 ) {
                StringBuilder errMessage = new StringBuilder(String.format("[%s#%s] RequestBody 를 가질 수 없는 Method 입니다.", this.restClientInterface.getSimpleName(), m.getName()));
                int initPathVariableSize = this.pathVariableNames.size();
                if ( this.hasPathVariable && (initPathVariableSize != pathValueSize) ) {
                    errMessage.append("\n");
                    errMessage.append(" - PathVariable 을 Url 에 선언한 경우, @PathVariable 어노테이션은 필수 입니다.");
                }
                if ( this.hasRequestParamAnnotation ) {
                    errMessage.append("\n");
                    errMessage.append(" - RequestParam 어노테이션을 선언한 경우, Get Method 혹은 Delete 메소드는 선언되어 있지 않은 파라미터에 대해서 RequestBody 로 취급합니다.");
                }
                throw new RestClientCallException(errMessage.toString());
            }
        }
    }

    private void checkListParamValid(@NotNull Method m) {
        Parameter[] parameters = m.getParameters();
        int paramCount = parameters.length;
        boolean hasQueryValue = this.queryValuesFrame.size() > 0;
        for (int i=0; i<paramCount; i++) {
            Parameter param = parameters[i];
            Class<?> paramClass = param.getType();
            boolean isList = paramClass.isAssignableFrom(List.class);
            boolean isMap = paramClass.isAssignableFrom(Map.class);
            boolean isPrimitiveInString = ClassUtil.isPrimitiveInString(paramClass);

            if ( isList ) {
                throw new RestClientCallException(String.format("[%s#%s] RestClient 는 List 타입의 파라미터를 지원하지 않습니다.", this.restClientInterface.getSimpleName(), m.getName()));
            }

            if ( hasQueryValue && this.queryValuesFrame.containsKey(i) && !isMap && !isPrimitiveInString ) {
                for ( Field x : paramClass.getDeclaredFields()) {
                    try {
                        paramClass.getMethod(ClassUtil.getGetterMethodByField(x));
                    } catch (NoSuchMethodException e) {
                        throw new RestClientCallException(String.format("[%s#%s] RestClient 에서 Reference 타입의 파라미터는 Getter 메소드는 필수입니다.", this.restClientInterface.getSimpleName(), m.getName()));
                    }
                }
            }
        }
    }
}
