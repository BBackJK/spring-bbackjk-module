package test.bbackjk.http.wrapper;


import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import test.bbackjk.http.helper.LogHelper;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class RequestMetadata {
    private final MediaType contentType;
    private final Map<String, String> headerValuesMap;
    private final Map<String, String> pathValuesMap;
    private final Map<String, String> queryValuesMap;
    private final String url;
    @Nullable
    private final Object bodyData;
    @Nullable
    private final Object[] args;
    private final LogHelper restClientLogger;

    public RequestMetadata(
            String url, MediaType mediaType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , @Nullable Object bodyData, @Nullable Object[] args
            , LogHelper restClientLogger
    ) {
        this.url = url;
        this.contentType = mediaType;
        this.headerValuesMap = headerValuesMap;
        this.pathValuesMap = pathValuesMap;
        this.queryValuesMap = queryValuesMap;
        this.bodyData = bodyData;
        this.args = args;
        this.restClientLogger = restClientLogger;
    }

    public RequestMetadata(String url, MediaType mediaType, LogHelper restClientLogger) {
        this(url, mediaType, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), null, null, restClientLogger);
    }

    public static RequestMetadata ofEmpty(String origin, String pathname, MediaType mediaType, LogHelper restClientLogger) {
        return new RequestMetadata(getFullUrl(origin, pathname), mediaType, restClientLogger);
    }

    public static RequestMetadata ofEmpty(String requestUrl, MediaType mediaType, LogHelper restClientLogger) {
        return new RequestMetadata(requestUrl, mediaType, restClientLogger);
    }

    public static RequestMetadata of(
            String requestUrl, MediaType mediaType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , Object bodyData, Object[] args
            , LogHelper restClientLogger
    ) {
        return new RequestMetadata(requestUrl, mediaType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger);
    }

    public static RequestMetadata of(
            String origin, String pathname, MediaType mediaType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , Object bodyData, Object[] args
            , LogHelper restClientLogger
    ) {
        return new RequestMetadata(getFullUrl(origin, pathname), mediaType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger);
    }

    public boolean isFormContent() {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(this.contentType);
    }

    public boolean isJsonContent() {
        return MediaType.APPLICATION_JSON.equalsTypeAndSubtype(this.contentType);
    }

    private static String getFullUrl(String origin, String pathname) {
        String copyOrigin = origin == null ? "" : origin;
        String copyPathname = pathname == null ? "" : pathname;

        if (copyOrigin.endsWith("/")) {
            if ( copyPathname.charAt(0) == '/') {
                return copyOrigin + copyPathname.substring(1);
            } else {
                return copyOrigin + copyPathname;
            }
        } else {
            if ( copyPathname.charAt(0) == '/') {
                return copyOrigin + copyPathname;
            } else {
                if ( copyOrigin.isBlank() && copyPathname.isBlank() ) {
                    return "";
                } else {
                    return copyOrigin + "/" + copyPathname;
                }
            }
        }
    }
}
