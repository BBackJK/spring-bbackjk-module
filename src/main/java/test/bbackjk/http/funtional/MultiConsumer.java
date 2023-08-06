package test.bbackjk.http.funtional;

public interface MultiConsumer<T, R> {
    void accept(T val1, R val2);
}
