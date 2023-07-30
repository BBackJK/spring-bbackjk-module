package test.bbackjk.http.wrapper;


import lombok.Getter;
import org.springframework.http.MediaType;
import test.bbackjk.http.util.RestMapUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RequestMetadata {
    private final String url;
    private final MediaType contentType;
    private final Map<String, String> headerValuesMap;
    private final Map<String, String> pathValuesMap;
    private final Map<String, String> queryValuesMap;
    private final Object bodyData;

    public RequestMetadata(Object[] args) {
        this.url = "";
        this.contentType = MediaType.APPLICATION_JSON;
        this.headerValuesMap = RestMapUtils.toReadonly(new HashMap<>());
        this.pathValuesMap = RestMapUtils.toReadonly(new HashMap<>());
        this.queryValuesMap = RestMapUtils.toReadonly(new HashMap<>());
        this.bodyData = null;
    }
}
