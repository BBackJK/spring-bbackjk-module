package test.bbackjk.http.exceptions;

public class RestClientDataMappingException extends RestClientCommonException {

    public RestClientDataMappingException(String msg) {
        super(msg);
    }

    public RestClientDataMappingException(Throwable e) {
        super(e);
    }
}
