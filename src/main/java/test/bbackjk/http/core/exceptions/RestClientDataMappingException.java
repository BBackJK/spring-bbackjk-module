package test.bbackjk.http.core.exceptions;

public class RestClientDataMappingException extends RestClientCommonException {

    public RestClientDataMappingException(String msg) {
        super(msg);
    }

    public RestClientDataMappingException(Throwable e) {
        super(e);
    }
}
