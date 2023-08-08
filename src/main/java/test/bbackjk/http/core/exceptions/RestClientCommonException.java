package test.bbackjk.http.core.exceptions;

public class RestClientCommonException extends RuntimeException {

    public RestClientCommonException(String msg) {
        super(msg);
    }

    public RestClientCommonException(Throwable e) {
        super(e);
    }
}
