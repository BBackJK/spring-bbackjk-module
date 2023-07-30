package test.bbackjk.http.exceptions;

public class RestClientCallException extends RestClientCommonException {
    public RestClientCallException(String msg) {
        super(msg);
    }

    public RestClientCallException(Throwable e) {
        super(e);
    }
}
