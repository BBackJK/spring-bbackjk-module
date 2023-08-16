package test.bbackjk.http.core.reflector;

public interface ArgumentPresetMetadata<T> {

    T get();
    T set(String paramName, Object value);
}
