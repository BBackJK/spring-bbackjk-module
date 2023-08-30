package test.bbackjk.http.core.bean.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import test.bbackjk.http.core.configuration.RestClientConnectProperties;
import test.bbackjk.http.core.exceptions.RestClientCallException;
import test.bbackjk.http.core.helper.LogHelper;
import test.bbackjk.http.core.interfaces.HttpAgent;
import test.bbackjk.http.core.util.RestClientUtils;
import test.bbackjk.http.core.wrapper.RequestMetadata;
import test.bbackjk.http.core.wrapper.ResponseMetadata;

import java.util.Map;


@Component
@Slf4j
public class RestTemplateAgent implements HttpAgent {

    private static final String LOGGING_DELIMITER = "========================================================================================";

    private final RestTemplate template;
    private final ObjectMapper om;

    public RestTemplateAgent(RestClientConnectProperties connectProperties) {
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(connectProperties.getConnectPoolSize())
                .setMaxConnPerRoute(connectProperties.getConnectPoolPerRoute())
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectProperties.getConnectKeepAlive() * 1000);
        factory.setReadTimeout(connectProperties.getConnectKeepAlive() * 1000);
        factory.setConnectionRequestTimeout(connectProperties.getConnectKeepAlive() * 1000);

        this.template = new RestTemplate(factory);
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
        UriComponentsBuilder requestBuilder = UriComponentsBuilder.fromHttpUrl(this.handleUrl(requestMetadata.getUrl(), requestMetadata.getPathValuesMap()));
        this.handleQueryParameter(requestBuilder, requestMetadata.getQueryValuesMap());
        try {
            RequestEntity<?> requestEntity = new RequestEntity<>(
                    this.handleBody(requestMetadata.getBodyData(), requestMetadata.isFormContent(), requestMetadata.getRestClientLogger())
                    , this.handleHeader(requestMetadata.getHeaderValuesMap(), requestMetadata.getContentType())
                    , httpMethod
                    , requestBuilder.build().toUri()
                    , String.class
            );
            this.requestLogging(requestEntity, requestMetadata.getRestClientLogger());
            long requestAt = System.currentTimeMillis();
            ResponseEntity<String> response = this.template.exchange(requestEntity, String.class);
            long responseAt = System.currentTimeMillis();
            this.responseLogging(response, requestMetadata.getRestClientLogger(), responseAt - requestAt);
            MediaType mediaType = response.getHeaders().getContentType();
            return new ResponseMetadata(
                    response.getStatusCodeValue()
                    , response.getBody()
                    , mediaType == null ? RestClientUtils.HEADER_CONTENT_TYPE_DEFAULT : mediaType.toString()
            );
        } catch (RestClientResponseException e) {
            String contentType = RestClientUtils.HEADER_CONTENT_TYPE_DEFAULT;
            HttpHeaders headers = e.getResponseHeaders();
            if ( headers != null ) {
                MediaType m = headers.getContentType();
                if ( m != null ) {
                    contentType = m.toString();
                }
            }
            return new ResponseMetadata(
                    e.getRawStatusCode()
                    , e.getMessage()
                    , contentType
            );
        }
    }

    private String handleUrl(String requestUrl, Map<String, String> pathValues) {
        if (pathValues.isEmpty()) {
            return requestUrl;
        }
        for (Map.Entry<String, String> x : pathValues.entrySet()) {
            requestUrl = requestUrl.replaceAll("\\{" + x.getKey() + "}", x.getValue());
        }
        return requestUrl;
    }

    private void handleQueryParameter(UriComponentsBuilder requestBuilder, Map<String, String> queryValues) {
        if (!queryValues.isEmpty()) {
            for (Map.Entry<String, String> kv : queryValues.entrySet()) {
                String val = kv.getValue();
                if ( val != null ) {
                    requestBuilder.queryParam(kv.getKey(), kv.getValue());
                }
            }
        }
    }

    private HttpHeaders handleHeader(Map<String, String> headerValues, MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        if (!headerValues.isEmpty()) {
            for (Map.Entry<String, String> kv : headerValues.entrySet()) {
                String key = kv.getKey();
                String val = kv.getValue();
                if ( key != null && val != null ) {
                    headers.add(key, val);
                }
            }
        }
        headers.add(HttpHeaders.CONTENT_TYPE, contentType.toString());
        return headers;
    }

    private Object handleBody(Object body, boolean isUrlEncodedForm, LogHelper logger) {
        if (body != null && isUrlEncodedForm) {
            try {
                MultiValueMap<String, String> trg = new LinkedMultiValueMap<>();
                Map<?,?> src = this.om.convertValue(body, Map.class);

                for (Map.Entry<?, ?> kv : src.entrySet()) {
                    Object key = kv.getKey();
                    Object value = kv.getValue();
                    if (key != null && value != null) {
                        trg.add(String.valueOf(key), String.valueOf(value));
                    }
                }
                return trg;
            } catch (IllegalArgumentException e) {
                logger.warn(e.getMessage());
                return body;
            }
        } else {
            return body;
        }
    }

    private void requestLogging(RequestEntity<?> request, LogHelper logger) {
        int headerSize = request.getHeaders().size();
        HttpMethod httpMethod = request.getMethod();

        logger.log("Request\t\t{}", LOGGING_DELIMITER);
        logger.log("Request\t\t| Agent\t\t\t\t: {}", this.getClass().getSimpleName());
        logger.log("Request\t\t| Url\t\t\t\t: {} {} ", httpMethod == null ? "N/A" : httpMethod.name(), request.getUrl());
        if (headerSize < 1) {
            logger.log("Request\t\t| Header\t\t\t: EMPTY");
        } else {
            request.getHeaders().forEach((k, listV) -> logger.log("Request\t\t| Header\t\t\t: {} - {}", k, listV));
        }

        if (request.getBody() != null) {
            logger.log("Request\t\t| Body\t\t\t\t: {}", request.getBody());
        }
        logger.log("Request\t\t{}", LOGGING_DELIMITER);
    }

    private void responseLogging(ResponseEntity<String> response, LogHelper logger, long callTimeDiff) {
        HttpHeaders headers = response.getHeaders();
        int headerSize = headers.size();

        logger.log("Response\t\t{}", LOGGING_DELIMITER);
        logger.log("Response\t\t| Agent\t\t\t\t: {}", this.getClass().getSimpleName());
        logger.log("Response\t\t| Total Call Millis\t: {} ms", callTimeDiff);
        logger.log("Response\t\t| Data(String)\t\t: {}", response.getBody());
        if (headerSize < 1) {
            logger.log("Response\t\t| Header\t\t\t: EMPTY");
        } else {
            headers.forEach((k, listV) -> logger.log("Response\t\t| Header\t\t\t: {} - {}", k, listV));
        }
        logger.log("Response\t\t{}", LOGGING_DELIMITER);
    }
}
