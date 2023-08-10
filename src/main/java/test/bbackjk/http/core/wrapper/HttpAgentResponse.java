package test.bbackjk.http.core.wrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import test.bbackjk.http.core.util.RestClientUtils;

import java.util.Map;

@Getter
public class HttpAgentResponse {
    private static final String DEFAULT_ERROR_MESSAGE = "요청한 서버에서 에러가 발생하였습니다.";
    private final int httpCode;
    private final boolean success;
    private final String stringResponse;
    private final ObjectMapper om;

    private HttpAgentResponse(int httpCode, boolean success, String stringResponse, ObjectMapper om) {
        this.httpCode = httpCode;
        this.success = success;
        this.stringResponse = stringResponse;
        this.om = om;
    }

    public HttpAgentResponse(int httpCode, String stringResponse, ObjectMapper om) {
        this(httpCode, RestClientUtils.isSuccess(httpCode), stringResponse, om);
    }

    public String getFailMessage() {
        if ( this.success ) {
            return "";
        }
        StringBuilder sb = new StringBuilder(String.valueOf(httpCode));
        if ( stringResponse != null && !stringResponse.isBlank() ) {
            try {
                Map<?, ?> jsonMap = this.om.readValue(stringResponse, Map.class);
                String result = DEFAULT_ERROR_MESSAGE;
                String message = (String) jsonMap.get("message");
                String msg = (String) jsonMap.get("msg");
                String error = (String) jsonMap.get("error");
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
