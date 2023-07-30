package test.bbackjk.http.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class Logs {

    private final String PREFIX = "[RestClient]";

    private String getFormatMessage(String msg) {
        return String.format("%s %s", PREFIX, String.format(msg));
    }

    public void log(String message, Object... args) {
        log.info(getFormatMessage(message), args);
    }

    public void log(String message) {
        log(message, (Object) null);
    }

    public void warn(String message, Object... args) {
        log.warn(getFormatMessage(message), args);
    }

    public void warn(String message) {
        warn(message, (Object) null);
    }

    public void err(String message, Object... args) {
        log.error(getFormatMessage(message), args);
    }

    public void err(String message) {
        err(message, (Object) null);
    }

}
