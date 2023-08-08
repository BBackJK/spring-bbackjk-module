package test.bbackjk.http.core.exceptions;

public class RestClientCallException extends RestClientCommonException {
    public RestClientCallException(String msg) {
        super(msg);
    }

    public RestClientCallException() {
        super("Http 요청을 실패하였습니다.");
    }

    public RestClientCallException(Throwable e) {
        super(e);
    }
}
