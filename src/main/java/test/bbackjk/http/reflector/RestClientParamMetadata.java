package test.bbackjk.http.reflector;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import test.bbackjk.http.interfaces.RestCallback;
import test.bbackjk.http.util.ClassUtil;
import test.bbackjk.http.util.ReflectorUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RestClientParamMetadata {

    private static final List<Class<? extends Annotation>> ALLOWED_PARAMETER_ANNOTATIONS = Stream.of(RequestParam.class, RequestHeader.class, RequestBody.class, PathVariable.class).collect(Collectors.toUnmodifiableList());
    private final Class<?> paramClass;
    private final Annotation annotation;
    @Getter
    private final String paramName;
    public RestClientParamMetadata(@NotNull Parameter parameter) {
        this.paramClass = parameter.getType();
        this.annotation = this.parseAnnotation(parameter);
        this.paramName = this.parseParamName(parameter, this.annotation);
    }

    public boolean isRestCallback() {
        return RestCallback.class.equals(this.paramClass);
    }

    public boolean isRequestParamAnnotation() {
        return this.annotation != null && RequestParam.class.equals(this.annotation.annotationType());
    }

    public boolean isRequestHeaderAnnotation() {
        return this.annotation != null && RequestHeader.class.equals(this.annotation.annotationType());
    }

    public boolean isPathVariableAnnotation() {
        return this.annotation != null && PathVariable.class.equals(this.annotation.annotationType());
    }

    public boolean isRequestBodyAnnotation() {
        return this.annotation != null && RequestBody.class.equals(this.annotation.annotationType());
    }

    public boolean isList() {
        return this.paramClass.isAssignableFrom(List.class);
    }

    public boolean isMap() {
        return this.paramClass.isAssignableFrom(Map.class);
    }

    public boolean isPrimitiveInString() {
        return ClassUtil.isPrimitiveInString(this.paramClass);
    }

    public boolean isReference() {
        return !this.isList() && !this.isMap() && !this.isPrimitiveInString();
    }

    @Nullable
    public Annotation getAnnotation() {
        return this.annotation;
    }

    public boolean hasAnnotation() {
        return this.annotation != null;
    }

    public boolean hasAllGetterField() {
        Field[] fields = this.paramClass.getDeclaredFields();
        int fieldCount = fields.length;
        for (int i=0; i<fieldCount; i++) {
            try {
                this.paramClass.getMethod(ClassUtil.getGetterMethodByField(fields[i]));
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
        return true;
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
        if ( this.hasAnnotation() && !this.isRequestBodyAnnotation() ) {
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
