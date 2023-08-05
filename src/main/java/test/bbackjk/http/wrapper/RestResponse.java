package test.bbackjk.http.wrapper;

import lombok.Getter;
import test.bbackjk.http.util.RestClientUtils;

@Getter
public class RestResponse {
    private final int httpCode;
    private final boolean success;
    private final String jsonString;
    private final String message;

    public RestResponse(int httpCode, String jsonString, String message) {
        this(httpCode, RestClientUtils.isSuccess(httpCode), jsonString, message);
    }

    public RestResponse(int httpCode, boolean success, String jsonString, String message) {
        this.httpCode = httpCode;
        this.success = success;
        this.jsonString = jsonString;
        this.message = message;
    }
}
