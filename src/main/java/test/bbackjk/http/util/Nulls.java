package test.bbackjk.http.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Nulls {

    public <T> T orElse(T origin, T replace) {
        return origin == null ? replace : origin;
    }
}
