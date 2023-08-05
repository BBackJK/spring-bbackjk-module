package test.bbackjk.http.reflector;

import lombok.Getter;

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
    private final Class<?> returnRawType;

    public RequestReturnResolver(Method method) {
        Class<?> returnClazz = method.getReturnType();
        ParameterizedType parameterizedType = this.getParameterizedType(method.getGenericReturnType());
        this.returnRawType = parameterizedType == null ? method.getReturnType() : (Class<?>) parameterizedType.getActualTypeArguments()[0];
        this.returnsList = returnClazz.isAssignableFrom(List.class);
        this.returnsMap = returnClazz.isAssignableFrom(Map.class);
        this.returnsVoid = void.class.equals(this.returnRawType);
        this.returnsString = String.class.equals(this.returnRawType);
        this.returnsOptional = Optional.class.equals(this.returnRawType);
    }

    private ParameterizedType getParameterizedType(Type returnType) {
       return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
