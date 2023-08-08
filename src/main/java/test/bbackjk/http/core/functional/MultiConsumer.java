package test.bbackjk.http.core.functional;

public interface MultiConsumer<T, R> {
    void accept(T val1, R val2);
}
