package test.bbackjk.http.core.reflector;

import lombok.Getter;
import org.apache.el.stream.Optional;
import org.jetbrains.annotations.Nullable;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.wrapper.RestResponse;
import test.bbackjk.http.core.exceptions.RestClientCommonException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

class RequestReturnMetadata {
    private static final LogHelper LOGGER = LogHelper.of(RequestReturnMetadata.class);
    private final Class<?> returnClass;
    @Getter
    private final Class<?> rawType;
    @Getter
    @Nullable
    private final Class<?> secondRawType;

    public RequestReturnMetadata(Method method) {
        this.returnClass = method.getReturnType();
        ParameterizedType parameterizedType = this.getParameterType(method.getGenericReturnType());
        if ( parameterizedType != null ) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            ParameterizedType overParameterType = this.getParameterType(actualType);
            if ( overParameterType != null ) {
                Type overActualType = overParameterType.getActualTypeArguments()[0];
                if ( overActualType instanceof ParameterizedType ) {
                    throw new RestClientCommonException(" Return 타입으로는 3번 이상 Wrapping 할 수 없습니다. (최대 2개 지원). ");
                }
                this.rawType = (Class<?>) overActualType;
                this.secondRawType = (Class<?>) overParameterType.getRawType();
            } else {
                this.rawType = (Class<?>) actualType;
                this.secondRawType = this.returnClass;
            }
        } else {
            this.rawType = this.returnClass;
            this.secondRawType = null;
        }
    }

    public boolean isWrap() {
        return isWrapList() || isWrapRestResponse() || isDoubleWrap() || isWrapMap() || isWrapOptional();
    }

    public boolean isWrapList() {
        return this.isWrapList(this.returnClass);
    }

    public boolean isWrapList(Class<?> clazz) {
        return clazz != null && clazz.isAssignableFrom(List.class);
    }

    public boolean isWrapMap() {
        return this.returnClass.isAssignableFrom(Map.class);
    }

    public boolean isVoid() {
        return void.class.equals(this.returnClass);
    }

    public boolean isWrapOptional() {
        return Optional.class.equals(this.returnClass);
    }

    public boolean isString() {
        return String.class.equals(this.returnClass);
    }

    public boolean isWrapRestResponse() {
        return RestResponse.class.equals(this.returnClass);
    }

    public boolean isDoubleWrap() {
        return this.secondRawType != null;
    }

    private ParameterizedType getParameterType(Type returnType) {
        return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
