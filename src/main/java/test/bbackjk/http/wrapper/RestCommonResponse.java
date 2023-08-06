package test.bbackjk.http.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import test.bbackjk.http.util.RestClientUtils;

import java.util.Map;

@Getter
public class RestCommonResponse {
    private static final String DEFAULT_ERROR_MESSAGE = "요청한 서버에서 에러가 발생하였습니다.";
    private final int httpCode;
    private final boolean success;
    private final String jsonString;
    private final ObjectMapper om;

    public RestCommonResponse(int httpCode, String jsonString, ObjectMapper om) {
        this(httpCode, RestClientUtils.isSuccess(httpCode), jsonString, om);
    }

    public RestCommonResponse(int httpCode, boolean success, String jsonString, ObjectMapper om) {
        this.httpCode = httpCode;
        this.success = success;
        this.jsonString = jsonString;
        this.om = om;
    }

    public String getFailMessage() {
        if ( this.success ) {
            return "";
        }
        StringBuffer sb = new StringBuffer(String.valueOf(httpCode));
        if ( jsonString != null && !jsonString.isBlank() ) {
            try {
                Map<String, String> jsonMap = this.om.readValue(jsonString, Map.class);
                String result = DEFAULT_ERROR_MESSAGE;
                String message = jsonMap.get("message");
                String msg = jsonMap.get("msg");
                String error = jsonMap.get("error");
                if ( message != null && !message.isBlank() ) {
                    result = message;
                } else if ( msg != null && !msg.isBlank() ) {
                    result = msg;
                } else if ( error != null && !error.isBlank() ) {
                    result = error;
                }

                return sb.append(" ")
                        .append(result).toString();

            } catch (JsonProcessingException e) {
                return sb.append(" ")
                        .append(DEFAULT_ERROR_MESSAGE).toString();
            }
        } else {
            return sb.append(" ")
                    .append(DEFAULT_ERROR_MESSAGE).toString();
        }
    }
}
