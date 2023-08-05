package test.bbackjk.http.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

@UtilityClass
@Slf4j
public class ReflectorUtils {

    @Nullable
    public Object annotationMethodInvoke(Annotation target, String invokeMethodName) {
        return annotationMethodInvoke(target, invokeMethodName, null);
    }

    @Nullable
    public Object annotationMethodInvoke(Annotation target, String invokeMethodName, Object[] args) {
        Class<? extends Annotation> annotationClazz = target.annotationType();
        try {
            return args == null || args.length == 0
                    ? annotationClazz.getMethod(invokeMethodName).invoke(target)
                    : annotationClazz.getMethod(invokeMethodName).invoke(target, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.warn(e.getMessage());
            return null;
        }
    }
}
