package test.bbackjk.http.core.reflector;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.core.interfaces.RestCallback;
import test.bbackjk.http.core.util.ClassUtil;
import test.bbackjk.http.core.util.ReflectorUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RequestParamMetadata {

    private static final List<Class<? extends Annotation>> ALLOWED_PARAMETER_ANNOTATIONS = Stream.of(RequestParam.class, RequestHeader.class, RequestBody.class, PathVariable.class).collect(Collectors.toUnmodifiableList());
    private final Class<?> paramClass;
    @Getter
    private final List<String> getterMethodNames;
    private final Annotation annotation;
    @Getter
    private final String paramName;
    public RequestParamMetadata(@NotNull Parameter parameter) {
        this.paramClass = parameter.getType();
        this.getterMethodNames = ClassUtil.getHasGetterFieldNameByClass(this.paramClass);
        this.annotation = this.parseAnnotation(parameter);
        this.paramName = this.parseParamName(parameter, this.annotation);
    }

    public boolean isRestCallback() {
        return RestCallback.class.equals(this.paramClass);
    }

    public boolean isAnnotationRequestParam() {
        return this.annotation != null && RequestParam.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationRequestHeader() {
        return this.annotation != null && RequestHeader.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationPathVariable() {
        return this.annotation != null && PathVariable.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationRequestBody() {
        return this.annotation != null && RequestBody.class.equals(this.annotation.annotationType());
    }

    public boolean isListType() {
        return this.paramClass.isAssignableFrom(List.class);
    }

    // TODO: private 고려
    public boolean isMapType() {
        return this.paramClass.isAssignableFrom(Map.class);
    }

    public boolean isPrimitiveInString() {
        return ClassUtil.isPrimitiveInString(this.paramClass);
    }

    public boolean isReferenceType() {
        return !this.isListType() && !this.isMapType() && !this.isPrimitiveInString() && !this.isRestCallback();
    }

    public boolean canRequestParam(boolean isOnlyRequestParam, boolean isEmptyAllAnnotation, List<String> pathValueNames) {
        if ( isOnlyRequestParam ) {
            return isAnnotationRequestParam();
        }

        if ( isEmptyAllAnnotation ) {
            return !pathValueNames.contains(this.paramName);
        }

        return !this.hasAnnotation() && !pathValueNames.contains(this.paramName) && !this.isRestCallback();
    }

    @Nullable
    public Annotation getAnnotation() {
        return this.annotation;
    }

    public boolean hasAnnotation() {
        return this.annotation != null;
    }

    @Nullable
    private Annotation parseAnnotation(Parameter parameter) {
        for (Class<? extends Annotation> a : ALLOWED_PARAMETER_ANNOTATIONS) {
            Annotation anno = parameter.getAnnotation(a);
            if ( anno != null ) {
                return anno;
            }
        }
        return null;
    }

    @NotNull
    private String parseParamName(Parameter parameter, Annotation annotation) {
        if ( this.hasAnnotation() && !RequestBody.class.equals(annotation.annotationType()) ) {
            String result = null;
            String value = (String) ReflectorUtils.annotationMethodInvoke(annotation, "value");
            String name = (String) ReflectorUtils.annotationMethodInvoke(annotation, "name");
            if ( name != null && !name.isBlank() ) {
                result = name;
            } else if ( value != null && !value.isBlank() ) {
                result = value;
            }
            return result == null ? parameter.getName() : result;
        }
        return parameter.getName();
    }
}
