package test.bbackjk.http.core.util;

import lombok.experimental.UtilityClass;
import test.bbackjk.http.core.wrapper.RequestMetadata;

import java.util.Arrays;
import java.util.Map;

@UtilityClass
public class RestClientUtils {

    public boolean isSuccess(int httpCode) {
        return httpCode / 100 == 2;
    }

    public String getParseUrl(RequestMetadata metadata) {
        Map<String, String> pathMap = metadata.getPathValuesMap();
        String result = metadata.getUrl();
        if (pathMap.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, String> x : pathMap.entrySet()) {
            result = result.replaceAll("\\{" + x.getKey() + "}", x.getValue());
        }
        return result;
    }

    public <T> T orElse(T origin, T replace) {
        return origin == null ? replace : origin;
    }

    public boolean orEqualsEnum(Enum<?> target, Enum<?>... enums) {
        return target != null && enums != null && enums.length != 0 && Arrays.stream(enums).anyMatch(e -> e == target);
    }
}
