package test.bbackjk.http.core.reflector;

import lombok.Getter;
import org.springframework.util.MethodInvoker;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
class QueryValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public QueryValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> {
            boolean isCollection = o instanceof Collection;
            boolean isMap = o instanceof Map;

            if (isCollection) {
                throw new RestClientCallException("RestClient 는 List 타입의 파라미터는 지원하지 않습니다.");
            }

            if (isMap) {
                Map<?, ?> map = (Map<?, ?>) o;
                map.forEach((k, v) -> preset.set(String.valueOf(k), v == null ? null : String.valueOf(v)));
            } else if (this.metadata.isReferenceType()) {
                MethodInvoker mi = new MethodInvoker();
                mi.setTargetObject(o);
                List<String> getterMethods = this.metadata.getGetterMethodNames();
                for ( String fieldName : getterMethods ) {
                    if ( fieldName != null ) {
                        try {
                            mi.setTargetMethod(ClassUtil.getGetterMethodByFieldName(fieldName));
                            mi.prepare();
                            Object v = mi.invoke();
                            if ( v != null ) {
                                preset.set(fieldName, String.valueOf(v));
                            }
                        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                                 IllegalAccessException e) {
                            // ignore..
                        }
                    }
                }
            } else {
                preset.set(metadata.getParamName(), String.valueOf(o));
            }
        });
    }
}
