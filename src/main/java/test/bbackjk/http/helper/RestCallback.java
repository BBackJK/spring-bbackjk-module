package test.bbackjk.http.helper;

public interface RestCallback<T> {

    void onSuccess(int httpCode, T data);
    void onFailure(int httpCode, String errorMessage);
}
