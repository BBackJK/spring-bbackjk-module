package test.bbackjk.http.core.wrapper;


import lombok.Getter;
import lombok.ToString;
import test.bbackjk.http.core.functional.MultiConsumer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@ToString
public class RestResponse<T> {

    private final T data;
    private final boolean success;
    private final int httpCode;
    private final String message;


    public RestResponse(T data, boolean success, int httpCode, String message) {
        this.data = data;
        this.success = success;
        this.httpCode = httpCode;
        this.message = message;
    }

    public static <T> RestResponse<T> success(T data, int httpCode) {
        return new RestResponse<>(data, true, httpCode, "");
    }

    public static <T> RestResponse<T> fail(int httpCode, String message) {
        return new RestResponse<>(null, false, httpCode, message);
    }

    public RestResponse<T> ifSuccess(Consumer<T> consumer) {
        if ( this.success ) {
            consumer.accept(this.data);
        }
        return this;
    }

    public RestResponse<T> ifFailure(MultiConsumer<Integer, String> consumer) {
        if ( !this.success ) {
            consumer.accept(this.httpCode, this.message);
        }
        return this;
    }

    public <R extends T> Optional<R> ifSuccess(Function<T, R> function) {
        if ( this.success ) {
            return Optional.ofNullable(function.apply(this.data));
        } else {
            return Optional.empty();
        }
    }

    public RestResponse<T> ifSuccess(Runnable runnable) {
        if ( this.success ) {
            runnable.run();
        }
        return this;
    }

    public RestResponse<T> ifFailure(Runnable runnable) {
        if ( !this.success ) {
            runnable.run();
        }
        return this;
    }
}
