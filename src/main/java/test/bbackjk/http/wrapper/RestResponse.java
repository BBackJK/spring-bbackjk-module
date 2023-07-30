package test.bbackjk.http.wrapper;

import lombok.Getter;
import test.bbackjk.http.util.RestUtils;

@Getter
public class RestResponse {
    private final int httpCode;
    private final boolean success;
    private final Object data;

    public RestResponse(int httpCode, Object data) {
        this(httpCode, RestUtils.isSuccess(httpCode), data);
    }

    public RestResponse(int httpCode, boolean success, Object data) {
        this.httpCode = httpCode;
        this.success = success;
        this.data = data;
    }
}
