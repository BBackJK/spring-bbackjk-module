package test.bbackjk.http.wrapper;


import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class RequestMetadata {
    private final MediaType contentType;
    private final Map<String, String> headerValuesMap;
    private final Map<String, String> pathValuesMap;
    private final Map<String, String> queryValuesMap;
    @Nullable
    private final Object bodyData;
    private final String url;
    @Nullable
    private final Object[] args;

    public RequestMetadata(String url, MediaType mediaType) {
        this.url = url;
        this.contentType = mediaType;
        this.headerValuesMap = new LinkedHashMap<>();
        this.pathValuesMap = new LinkedHashMap<>();
        this.queryValuesMap = new LinkedHashMap<>();
        this.bodyData = null;
        this.args = null;
    }

    public RequestMetadata(
            String url
            , MediaType mediaType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , @Nullable Object bodyData
            , @Nullable Object[] args
    ) {
        this.url = url;
        this.contentType = mediaType;
        this.headerValuesMap = headerValuesMap;
        this.pathValuesMap = pathValuesMap;
        this.queryValuesMap = queryValuesMap;
        this.bodyData = bodyData;
        this.args = args;
    }

    public static RequestMetadata ofEmpty(String origin, String pathname, MediaType mediaType) {
        return new RequestMetadata(getFullUrl(origin, pathname), mediaType);
    }

    public static RequestMetadata of(
            String origin, String pathname, MediaType mediaType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , Object bodyData
            , Object[] args
    ) {
        return new RequestMetadata(getFullUrl(origin, pathname), mediaType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args);
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
