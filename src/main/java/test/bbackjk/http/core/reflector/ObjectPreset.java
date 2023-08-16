package test.bbackjk.http.core.reflector;

public class ObjectPreset implements ArgumentPresetMetadata<Object> {

    private Object bodyData = null;

    @Override
    public Object get() {
        return this.bodyData;
    }

    @Override
    public synchronized Object set(String paramName, Object value) {
        this.bodyData = value;
        return this.bodyData;
    }
}
