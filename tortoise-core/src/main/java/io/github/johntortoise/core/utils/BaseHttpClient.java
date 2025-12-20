package io.github.johntortoise.core.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.consts.HTTPConst;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;

@Data
public class BaseHttpClient {

    protected final OkHttpClient client;
    protected final String baseUrl;
    protected final ObjectMapper objectMapper;
    protected final String sk;
    protected final String skHeaderName;

    public BaseHttpClient(OkHttpClient client, String baseUrl, String sk) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.sk = sk;
        this.skHeaderName = HTTPConst.X_API_KEY;
    }


    protected Request.Builder createRequestBuilder() {
        Request.Builder builder = new Request.Builder();
        if (sk != null && !sk.trim().isEmpty()) {
            builder.header(skHeaderName, sk);
        }
        return builder;
    }


    protected <T> T doGet(String path, Class<T> responseType) throws IOException {
        return doGet(path, null, responseType);
    }


    protected <T> T doGet(String path, HttpUrl.Builder urlBuilder, Class<T> responseType) {
        try {
            HttpUrl url = (urlBuilder != null) ? urlBuilder.build() :
                    HttpUrl.parse(baseUrl + path);

            Request request = createRequestBuilder()
                    .url(url)
                    .get()
                    .build();

            return executeRequest(request, responseType);
        } catch (Exception e) {
            LogUtil.error("调用GET请求异常: " + path, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }


    protected <T> T doGet(String path, HttpUrl.Builder urlBuilder, Type responseType) {
        try {
            HttpUrl url = (urlBuilder != null) ? urlBuilder.build() :
                    HttpUrl.parse(baseUrl + path);

            Request request = createRequestBuilder()
                    .url(url)
                    .get()
                    .build();

            return executeRequest(request, responseType);
        } catch (Exception e) {
            LogUtil.error("调用GET请求异常: " + path, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }


    protected <T> T doPost(String path, Object requestBody, Class<T> responseType) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(
                    json,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = createRequestBuilder()
                    .url(baseUrl + path)
                    .post(body)
                    .build();

            return executeRequest(request, responseType);
        } catch (Exception e) {
            LogUtil.error("调用POST请求异常: " + path, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }


    protected void doPost(String path, Object requestBody) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(
                    json,
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = createRequestBuilder()
                    .url(baseUrl + path)
                    .post(body)
                    .build();

            executeRequest(request);
        } catch (Exception e) {
            LogUtil.error("调用POST请求异常: " + path, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }


    protected Request.Builder createRequestBuilderWithHeaders(Headers headers) {
        Request.Builder builder = createRequestBuilder();
        if (headers != null) {
            builder.headers(headers);
        }
        return builder;
    }


    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败，状态码: " + response.code() +
                        ", 响应: " + (response.body() != null ? response.body().string() : "null"));
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseJson = responseBody.string();
                if (String.class.equals(responseType)) {
                    return responseType.cast(responseJson);
                }
                return objectMapper.readValue(responseJson, responseType);
            } else {
                throw new RuntimeException("响应体为空");
            }
        }
    }


    private <T> T executeRequest(Request request, Type responseType) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败，状态码: " + response.code() +
                        ", 响应: " + (response.body() != null ? response.body().string() : "null"));
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseJson = responseBody.string();
                return objectMapper.readValue(responseJson, objectMapper.getTypeFactory().constructType(responseType));
            } else {
                throw new RuntimeException("响应体为空");
            }
        }
    }


    private void executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("请求失败，状态码: " + response.code() +
                        ", 响应: " + (response.body() != null ? response.body().string() : "null"));
            }
        }
    }


    protected HttpUrl.Builder buildUrl(String path) {
        return HttpUrl.parse(baseUrl + path).newBuilder();
    }
}