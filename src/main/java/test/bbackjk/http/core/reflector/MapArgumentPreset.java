package test.bbackjk.http.core.reflector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapArgumentPreset implements ArgumentPresetMetadata<Map<String, String>> {

    Map<String, String> target = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> get() {
        return this.target;
    }

    @Override
    public Map<String, String> set(String paramName, Object value) {
        this.target.put(paramName, String.valueOf(value));
        return this.target;
    }
}
