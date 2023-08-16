package test.bbackjk.http.core.interfaces;

import java.util.concurrent.CompletableFuture;

public interface RestCallback<T> {

    void onSuccess(int httpCode, T data);
    void onFailure(int httpCode, String errorMessage);

    default boolean isAsync() {
        return true;
    }

    default void run(boolean success, int httpCode, T data, String errorMessage) {
        Runnable runnable = () -> {
            if (success) onSuccess(httpCode, data);
            else onFailure(httpCode, errorMessage);
        };

        if (isAsync()) {
            CompletableFuture.runAsync(runnable);
        } else {
            runnable.run();
        }
    }
}
