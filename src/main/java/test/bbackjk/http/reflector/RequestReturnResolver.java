package test.bbackjk.http.reflector;

import lombok.Getter;
import test.bbackjk.http.wrapper.RestResponse;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class RequestReturnResolver {

    private final boolean returnsList;
    private final boolean returnsMap;
    private final boolean returnsVoid;
    private final boolean returnsOptional;
    private final boolean returnsString;
    private final boolean returnRestResponse;
    private final Class<?> returnRawType;

    public RequestReturnResolver(Method method) {
        Class<?> returnClazz = method.getReturnType();
        ParameterizedType parameterizedType = this.getParameterizedType(method.getGenericReturnType());
        ParameterizedType secondParameterizedType = null;
        if ( parameterizedType == null ) {
            this.returnRawType = returnClazz;
        } else {
            Type findParameterType = parameterizedType.getActualTypeArguments()[0];
            secondParameterizedType = this.getParameterizedType(findParameterType);
            if ( secondParameterizedType == null ) {
                this.returnRawType = (Class<?>) findParameterType;
            } else {
                this.returnRawType = (Class<?>) secondParameterizedType.getActualTypeArguments()[0];
            }
        }

        this.returnsList = secondParameterizedType == null
                        ? returnClazz.isAssignableFrom(List.class)
                        : ((Class<?>) secondParameterizedType.getRawType()).isAssignableFrom(List.class);
        this.returnsMap = returnClazz.isAssignableFrom(Map.class);
        this.returnsVoid = void.class.equals(this.returnRawType);
        this.returnsString = String.class.equals(this.returnRawType);
        this.returnsOptional = Optional.class.equals(returnClazz);
        this.returnRestResponse = RestResponse.class.equals(returnClazz);
    }

    private ParameterizedType getParameterizedType(Type returnType) {
       return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
