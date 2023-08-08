package test.bbackjk.http.functional;

public interface MultiConsumer<T, R> {
    void accept(T val1, R val2);
}
