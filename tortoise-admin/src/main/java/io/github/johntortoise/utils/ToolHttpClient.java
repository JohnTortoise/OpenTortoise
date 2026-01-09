package io.github.johntortoise.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.johntortoise.core.dto.sys.HeadersDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.BaseHttpClient;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import okhttp3.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ToolHttpClient extends BaseHttpClient {

    public ToolHttpClient() {
        super(createDefaultClient());
    }

    private static OkHttpClient createDefaultClient() {
        return createClientBuilder().build();
    }

    protected static OkHttpClient.Builder createClientBuilder() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 10, TimeUnit.MINUTES))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
    }

    public Map<String,String> generateHeaders(List<HeadersDTO> headersDTOS){
        Map<String, String> headerMap = new HashMap<>();
        headersDTOS.forEach(value->{
            headerMap.put(value.getName(),value.getValue());
        });
        return headerMap;
    }


    public String callGetCurl(String toolName,String url, List<HeadersDTO> headersDTOS, String args) {
        try {
            return doGet(url,generateParamsForGet(args),generateHeaders(headersDTOS),String.class);
        }catch (Exception e){
            LogUtil.error("调用工具:{}出错",toolName,e);
            return e.getMessage();
        }
    }

    public String callPostCurl(String toolName,String url, List<HeadersDTO> headersDTOS, String args) {
        try {
            return doPost(url,args,generateHeaders(headersDTOS),String.class);
        }catch (Exception e){
            LogUtil.error("调用工具:{}出错",toolName,e);
            return e.getMessage();
        }
    }

    public Map<String,String> generateParamsForGet(String args) {
        Map<String,String> map = new HashMap<>();
        try {
            if (EmptyUtil.isNotEmpty(args)) {
                JsonNode jsonNode = ObjectMapperUtil.createObjectMapper().readTree(args);
                if (jsonNode != null && jsonNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        map.put(entry.getKey(), entry.getValue().asText());
                    }
                }
            }
            return map;
        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage());
        }
    }
}