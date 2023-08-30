package test.bbackjk.http.core.wrapper;

import lombok.Getter;
import lombok.ToString;
import test.bbackjk.http.core.util.ObjectUtils;
import test.bbackjk.http.core.util.RestClientUtils;

@Getter
@ToString
public class ResponseMetadata {
    private static final String DEFAULT_ERROR_MESSAGE = "요청한 서버에서 에러가 발생하였습니다.";
    private final int httpCode;
    private final boolean success;
    private final String stringResponse;
    private final String contentType;

    private ResponseMetadata(int httpCode, boolean success, String stringResponse, String contentType) {
        this.httpCode = httpCode;
        this.success = success;
        this.stringResponse = stringResponse;
        this.contentType = contentType;
    }

    public ResponseMetadata(int httpCode, String stringResponse, String contentType) {
        this(httpCode, RestClientUtils.isSuccess(httpCode), stringResponse, contentType);
    }

    public String getFailMessage() {
        if ( this.success ) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        return sb.append(ObjectUtils.isEmpty(stringResponse) ? DEFAULT_ERROR_MESSAGE : stringResponse).toString();
    }

    public boolean isXml() {
        // application/xml;charset=UTF-8
        if ( ObjectUtils.isEmpty(contentType) ) {
            return false;
        }
        String[] contentTypeSplits = contentType.split(";");
        if ( contentTypeSplits.length < 1 ) {
            return false;
        }
        return contentTypeSplits[0].endsWith("xml");
    }
}
