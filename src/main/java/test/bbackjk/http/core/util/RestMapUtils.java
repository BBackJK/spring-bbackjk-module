package test.bbackjk.http.core.util;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class RestMapUtils {

    public <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> invokeFunction) {
        V value = map.get(key);
        if ( value != null ) {
            return value;
        }
        return map.computeIfAbsent(key, invokeFunction);
    }

    public <K, V> Map<K, V> toReadonly(Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }
}
