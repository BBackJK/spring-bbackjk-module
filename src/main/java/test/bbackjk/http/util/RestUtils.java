package test.bbackjk.http.util;

import lombok.experimental.UtilityClass;
import test.bbackjk.http.wrapper.RequestMetadata;

import java.util.Map;

@UtilityClass
public class RestUtils {

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
}
