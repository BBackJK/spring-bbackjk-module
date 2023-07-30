package test.bbackjk.http.exceptions;

public class RestClientCommonException extends RuntimeException {

    public RestClientCommonException(String msg) {
        super(msg);
    }

    public RestClientCommonException(Throwable e) {
        super(e);
    }
}
