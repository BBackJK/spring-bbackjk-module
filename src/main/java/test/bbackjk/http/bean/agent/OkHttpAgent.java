package test.bbackjk.http.bean.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.springframework.stereotype.Component;
import test.bbackjk.http.configuration.RestClientConnectProperties;
import test.bbackjk.http.exceptions.RestClientCallException;
import test.bbackjk.http.helper.LogHelper;
import test.bbackjk.http.interfaces.HttpAgent;
import test.bbackjk.http.util.RestClientUtils;
import test.bbackjk.http.wrapper.RequestMetadata;
import test.bbackjk.http.wrapper.RestCommonResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class OkHttpAgent implements HttpAgent {
    private final OkHttpClient client;
    private final ObjectMapper om;

    public OkHttpAgent(RestClientConnectProperties connectProperties) {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
//        okHttpBuilder.addInterceptor(new OkHttpInterceptor());

        Duration timeout = Duration.ofSeconds(connectProperties.getTimout());

        // 연결하려는 서버와 connect 를 하는 제한 시간 설정 ( default 10s ) ==> SocketTimeoutException 발생
        okHttpBuilder.connectTimeout(timeout);
        // 호출한 서버의 응답을 기다리는 제한 시간 설정 ( default 10s ) ==> SocketTimeoutException 발생
        okHttpBuilder.readTimeout(timeout);
        // 서버를 호출 할 때 요청을 만드는 제한 시간 설정 ( default 10s ) ==> SocketTimeoutException 발생
        okHttpBuilder.writeTimeout(timeout);
        // 완전한 HTTP 호출에 대한 제한 시간 설정 ( DNS 확인, 연결, 요청 본문 작성, 서버 처리 및 응답 본문 읽기가 포함 ) ( default 10s ) ==> InterruptedIOException 발생
        okHttpBuilder.callTimeout(timeout);

        okHttpBuilder.connectionPool(
                new ConnectionPool(
                        connectProperties.getPoolSize()
                        , connectProperties.getKeepAlive()
                        , TimeUnit.MINUTES
                )
        );

        this.client = okHttpBuilder.build();
        this.om = new ObjectMapper();
    }


    @Override
    public RestCommonResponse doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        String url = RestClientUtils.getParseUrl(requestMetadata);

        HttpUrl.Builder httpUrlBuilder = HttpUrl.get(url).newBuilder();
        this.initQueryParameters(httpUrlBuilder, requestMetadata.getQueryValuesMap());
        Request.Builder requestBuilder = new Request.Builder()
                .get()
                .url(httpUrlBuilder.build());
        this.initHeaderValues(requestBuilder, requestMetadata.getHeaderValuesMap());

        Request request = requestBuilder.build();
        try (Response result = client.newCall(request).execute()) {
            this.logging(result, requestMetadata.getRestClientLogger());
            ResponseBody responseBody = result.body();
            return new RestCommonResponse(result.code(), responseBody == null ? "" : responseBody.string(), this.om);
        } catch (IOException e) {
            throw new RestClientCallException(e);
        }
    }

    @Override
    public RestCommonResponse doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        RequestBody requestBody = null;

        try {
            requestBody = this.getRequestBody(requestMetadata);
        } catch (JsonProcessingException e) {
            throw new RestClientCallException(e);
        }

        String url = RestClientUtils.getParseUrl(requestMetadata);

        HttpUrl.Builder httpUrlBuilder = HttpUrl.get(url).newBuilder();
        this.initQueryParameters(httpUrlBuilder, requestMetadata.getQueryValuesMap());
        Request.Builder requestBuilder = new Request.Builder()
                .post(requestBody)
                .url(httpUrlBuilder.build());
        this.initHeaderValues(requestBuilder, requestMetadata.getHeaderValuesMap());

        try (Response result = client.newCall(requestBuilder.build()).execute()) {
            this.logging(result, requestMetadata.getRestClientLogger());
            ResponseBody responseBody = result.body();
            String bodyString = responseBody == null ? "" :responseBody.string();
            return new RestCommonResponse(result.code(), bodyString, this.om);
        } catch (IOException e) {
            throw new RestClientCallException(e);
        }
    }

    @Override
    public RestCommonResponse doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    @Override
    public RestCommonResponse doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return null;
    }

    private void initQueryParameters(HttpUrl.Builder httpUrlBuilder, Map<String, String> queryValueMap) {
        if ( !queryValueMap.isEmpty() ) {
            for (Map.Entry<String, String> value : queryValueMap.entrySet()) {
                httpUrlBuilder.addQueryParameter(value.getKey(), value.getValue());
            }
        }
    }

    private void initHeaderValues(Request.Builder requestBuilder, Map<String, String> headerValueMap) {
        if ( !headerValueMap.isEmpty() ) {
            for (Map.Entry<String, String> value : headerValueMap.entrySet()) {
                requestBuilder.addHeader(value.getKey(), value.getValue());
            }
        }
    }

    private RequestBody getRequestBody(RequestMetadata requestMetadata) throws JsonProcessingException {
        if ( requestMetadata.isFormContent() ) {
            Map<?, ?> map = requestMetadata.getBodyData() == null
                    ? new HashMap<>()
                    : om.convertValue(requestMetadata.getBodyData(), Map.class);
            FormBody.Builder builder = new FormBody.Builder();
            map.forEach((k, v) -> builder.add(String.valueOf(k), String.valueOf(v)));
            return builder.build();
        } else {
            return RequestBody.create(
                    om.writeValueAsString(requestMetadata.getBodyData() == null ? new HashMap<>() : requestMetadata.getBodyData())
                    , this.parseMediaType(requestMetadata.getContentType())
            );
        }
    }

    private MediaType parseMediaType(org.springframework.http.MediaType requestContentType) {
        return MediaType.parse(requestContentType.getType() + "/" + requestContentType.getSubtype());
    }

    private void logging(Response response, LogHelper logger) {
        Request request = response.request();

        logger.log("Url          : {} {} {} ms", request.method(), request.url().url().toString(), response.receivedResponseAtMillis() - response.sentRequestAtMillis());
        request.headers().iterator().forEachRemaining(k -> logger.log("Header       : {}, {}", k.component1(), k.component2()));

        if (request.body() != null) {
            try (Buffer bf = new Buffer()) {
                request.body().writeTo(bf);
                logger.log("Body         : {}", bf.readString(Charset.defaultCharset()));
                logger.log("Content-Type : {}", request.body().contentType());
            } catch (IOException e) {
                logger.err(e.getMessage());
            }
        }
    }
}
