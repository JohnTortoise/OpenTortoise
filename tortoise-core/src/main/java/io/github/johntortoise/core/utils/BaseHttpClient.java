package io.github.johntortoise.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.consts.HTTPConst;
import io.github.johntortoise.core.dto.sys.HeadersDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import lombok.Data;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class BaseHttpClient {

    protected final OkHttpClient client;
    protected final ObjectMapper objectMapper;

    public BaseHttpClient(OkHttpClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected <T> T doGet(String url, Map<String,String> params,Map<String,String> headers,Class<T> responseType){
        try {
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            if(EmptyUtil.isNotEmpty(params)){
                params.forEach(urlBuilder::addQueryParameter);
            }
            Request.Builder requestBuilder = new Request.Builder();

            if(EmptyUtil.isNotEmpty(headers)){
                headers.forEach(requestBuilder::header);
            }

            Request request = requestBuilder.url(urlBuilder.build()).build();
            return executeRequest(request,responseType);
        }catch (Exception e){
            LogUtil.error("调用GET请求异常:{} ",url,e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }

    protected <T> T doPost(String url, Object params,Map<String,String> headers,Class<T> responseType){
        try {
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            Request.Builder requestBuilder = new Request.Builder();
            headers.forEach(requestBuilder::header);


            String requestBodyJson;
            if (params instanceof String) {
                String strParam = (String) params;
                validateJsonFormat(strParam);
                requestBodyJson = strParam;
            } else {
                requestBodyJson = objectMapper.writeValueAsString(params);
            }

            RequestBody body = RequestBody.create(
                    requestBodyJson,
                    MediaType.parse("application/json; charset=utf-8"));



            Request request = requestBuilder.url(urlBuilder.build()).post(body).build();
            return executeRequest(request,responseType);
        }catch (Exception e){
            LogUtil.error("调用POST请求异常:{} ",url,e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
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

    private void validateJsonFormat(String jsonStr){
        try {
            objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("传入的字符串不是合法的JSON格式：" + jsonStr, e);
        }
    }

}