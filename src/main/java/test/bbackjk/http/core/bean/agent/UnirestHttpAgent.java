package test.bbackjk.http.core.bean.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.*;
import kong.unirest.HttpMethod;
import kong.unirest.HttpRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import test.bbackjk.http.core.configuration.RestClientConnectProperties;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.util.RestClientUtils;
import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.wrapper.ResponseMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class UnirestHttpAgent implements HttpAgent {

    private static final String LOGGING_DELIMITER = "========================================================================================";
    private final UnirestInstance unirest;
    private final ObjectMapper om;

    public UnirestHttpAgent(RestClientConnectProperties connectProperties) {

        Config config = new Config();
        config.socketTimeout(connectProperties.getSocketTimeout() * 1000)
                .connectTimeout(connectProperties.getConnectKeepAlive() * 1000)
                .concurrency(
                        connectProperties.getConnectPoolSize()
                        , connectProperties.getConnectPoolPerRoute()
                )
                .followRedirects(false)
                .enableCookieManagement(false)
                .cacheResponses(
                        Cache.builder().depth(10)
                        .maxAge(10, TimeUnit.SECONDS)
                )
        ;

        this.unirest = new UnirestInstance(config);
        this.om = new ObjectMapper();
    }

    @Override
    public ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.GET);
    }

    @Override
    public ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.POST);
    }

    @Override
    public ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.PATCH);
    }

    @Override
    public ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.PUT);
    }

    @Override
    public ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.DELETE);
    }

    private ResponseMetadata doCall(RequestMetadata requestMetadata, HttpMethod httpMethod) {
        LogHelper logger = requestMetadata.getRestClientLogger();

        HttpRequestWithBody requestBuilder = this.unirest.request(httpMethod.name(), requestMetadata.getUrl());
        this.handleHeader(requestBuilder, requestMetadata.getHeaderValuesMap(), requestMetadata.getContentType());
        this.handlePathValue(requestBuilder, requestMetadata.getPathValuesMap());
        this.handleQueryParameter(requestBuilder, requestMetadata.getQueryValuesMap());
        HttpRequest<?> request = this.handleBody(requestBuilder, requestMetadata.getBodyData(), requestMetadata.isFormContent(), logger);

        try {
            this.requestLogging(request, requestMetadata.getRestClientLogger());
            long requestAt = System.currentTimeMillis();
            HttpResponse<String> response = request.asString();
            long responseAt = System.currentTimeMillis();
            this.responseLogging(response, requestMetadata.getRestClientLogger(), responseAt - requestAt);
            List<String> contentTypeList = response.getHeaders().get(RestClientUtils.HEADER_CONTENT_TYPE_KEY);
            return new ResponseMetadata(
                    response.getStatus()
                    , response.getBody()
                    , contentTypeList.isEmpty() ? RestClientUtils.HEADER_CONTENT_TYPE_DEFAULT : contentTypeList.get(0)
            );
        } catch (UnirestException e) {
            throw new RestClientCallException(e);
        }
    }

    private void handlePathValue(HttpRequestWithBody requestBuilder, Map<String, String> pathValues) {
        if (pathValues.isEmpty()) return;

        for (Map.Entry<String, String> x : pathValues.entrySet()) {
            String key = x.getKey();
            String value = x.getValue();
            if (key != null && value != null) {
                requestBuilder.routeParam(key, value);
            }
        }
    }

    private void handleQueryParameter(HttpRequestWithBody requestBuilder, Map<String, String> queryValues) {
        if (queryValues.isEmpty()) return;

        for (Map.Entry<String, String> x : queryValues.entrySet()) {
            String key = x.getKey();
            String value = x.getValue();
            if (key != null && value != null) {
                requestBuilder.queryString(key, value);
            }
        }
    }

    private void handleHeader(HttpRequestWithBody requestBuilder, Map<String, String> headerValues, MediaType contentType) {
        requestBuilder.header(HttpHeaders.CONTENT_TYPE, contentType.toString());
        if (!headerValues.isEmpty()) {
            for (Map.Entry<String, String> kv : headerValues.entrySet()) {
                String key = kv.getKey();
                String val = kv.getValue();
                if ( key != null && val != null ) {
                    requestBuilder.header(key, val);
                }
            }
        }
    }

    private HttpRequest<?> handleBody(HttpRequestWithBody requestBuilder, Object body, boolean isUrlEncodedForm, LogHelper logger) {
        if (body == null) return requestBuilder;

        if (isUrlEncodedForm) {
            try {
                Map<?,?> map = this.om.convertValue(body, Map.class);
                if (!map.isEmpty()) {
                    HttpRequest<MultipartBody> result = null;
                    for (Map.Entry<?, ?> kv : map.entrySet()) {
                        Object key = kv.getKey();
                        Object value = kv.getValue();
                        if (key != null && value != null) {
                            result = requestBuilder.field(String.valueOf(key), String.valueOf(value));
                        }
                    }
                    return result == null ? requestBuilder : result;
                } else {
                    return requestBuilder;
                }
            } catch (IllegalArgumentException e) {
                logger.warn(e.getMessage());
                return requestBuilder.body(body);
            }
        } else {
            return requestBuilder.body(body);
        }
    }

    private void requestLogging(HttpRequest<?> request, LogHelper logger) {
        Headers headers = request.getHeaders();
        int headerSize = headers.size();
        HttpMethod httpMethod = request.getHttpMethod();

        logger.log("Request\t\t{}", LOGGING_DELIMITER);
        logger.log("Request\t\t| Agent\t\t\t\t: {}", this.getClass().getSimpleName());
        logger.log("Request\t\t| Url\t\t\t\t: {} {} ", httpMethod == null ? "N/A" : httpMethod.name(), request.getUrl());

        if (headerSize < 1) {
            logger.log("Request\t\t| Header\t\t\t: EMPTY");
        } else {
            headers.all().forEach(h -> logger.log("Request\t\t| Header\t\t\t: {} - {}", h.getName(), h.getValue()));
        }


        request.getBody().ifPresent(b -> {
            if (b.isMultiPart()) {
                List<BodyPart> bodyParts = new ArrayList<>(b.multiParts());
                logger.log("Request\t\t| Body\t\t\t\t: {}", bodyParts);
            } else if (b.isEntityBody()) {
                logger.log("Request\t\t| Body\t\t\t\t: {}", b.uniPart());
            }
        });
        logger.log("Request\t\t{}", LOGGING_DELIMITER);
    }

    private void responseLogging(HttpResponse<String> response, LogHelper logger, long callTimeDiff) {
        Headers headers = response.getHeaders();
        int headerSize = headers.size();

        logger.log("Response\t\t{}", LOGGING_DELIMITER);
        logger.log("Response\t\t| Agent\t\t\t\t: {}", this.getClass().getSimpleName());
        logger.log("Response\t\t| Total Call Millis\t: {} ms", callTimeDiff);
        logger.log("Response\t\t| Data(String)\t\t: {}", response.getBody());
        if (headerSize < 1) {
            logger.log("Response\t\t| Header\t\t\t: EMPTY");
        } else {
            headers.all().forEach(h -> logger.log("Response\t\t| Header\t\t\t: {} - {}", h.getName(), h.getValue()));
        }
        logger.log("Response\t\t{}", LOGGING_DELIMITER);
    }
}
