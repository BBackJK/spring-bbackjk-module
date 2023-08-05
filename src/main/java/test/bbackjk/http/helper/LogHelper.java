package test.bbackjk.http.helper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogHelper {

    private final String context;

    public LogHelper(String context) {
        this.context = context;
    }

    public LogHelper(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public static LogHelper of(Class<?> clazz) {
        return of(clazz.getSimpleName());
    }

    public static LogHelper of(String context) {
        return new LogHelper(context);
    }

    public void log(String message, Object... args) {
        log.info(getFormatMessage(message, "LOG"), args);
    }

    public void log(String message) {
        log(message, (Object) null);
    }

    public void warn(String message, Object... args) {
        log.warn(getFormatMessage(message, "WARN"), args);
    }

    public void warn(String message) {
        warn(message, (Object) null);
    }

    public void err(String message, Object... args) {
        log.error(getFormatMessage(message, "ERROR"), args);
    }

    public void err(String message) {
        err(message, (Object) null);
    }

    private String getFormatMessage(String msg, String logLevel) {
        return String.format("[%s %s] %s", this.context, logLevel, msg);
    }
}
