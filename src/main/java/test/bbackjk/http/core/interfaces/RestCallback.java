package test.bbackjk.http.core.interfaces;

public interface RestCallback<T> {

    void onSuccess(int httpCode, T data);
    void onFailure(int httpCode, String errorMessage);
}
