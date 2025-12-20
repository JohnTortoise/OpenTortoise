package io.github.johntortoise.core.client.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.callback.StreamCallBack;
import io.github.johntortoise.core.client.llm.helper.RequestBuilder;
import io.github.johntortoise.core.client.llm.helper.ResponseBuilder;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.ConversationContext;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;



public class StreamLLMApiClient {
    protected final RequestBuilder requestBuilder;
    protected final ResponseBuilder responseBuilder;
    protected final OkHttpClient client;

    public StreamLLMApiClient(String baseUrl, String apiKey, String modelName, BigDecimal temperature) {
        ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
        this.requestBuilder = new RequestBuilder(baseUrl, apiKey, modelName, objectMapper,temperature);
        this.responseBuilder = new ResponseBuilder(objectMapper);

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();    }


    protected Request buildChatRequest(List<Message> messages, String conversationId) {
        return requestBuilder.buildChatRequest(messages, true, conversationId);
    }

    public static void invoke(ConversationContext context, StreamCallBack customerCallBack, StreamCallBack sysCallBack) {
        try {
            Model modelConfig = context.getModelConfig();
            StreamLLMApiClient streamLLMApiClient = createClient(modelConfig);
            streamLLMApiClient.chatCompletion(context.getMessages(), context.getConversationId(),
                    customerCallBack, sysCallBack);
        } catch (Exception e) {
            throw new TortoiseBusinessException(ErrorCodeEnum.INVOKE_LLM_ERROR, e.getMessage());
        }
    }

    public static StreamLLMApiClient createClient(Model modelConfig){
        return new StreamLLMApiClient(
                modelConfig.getCompleteUrl(),
                modelConfig.getApiKey(),
                modelConfig.getModelName(),
                modelConfig.getTemperature()
        );
    }

    public final void chatCompletion(List<Message> messages, String conversationId,
                                     StreamCallBack customerCallBack, StreamCallBack sysCallBack) throws IOException {
        Request request =  buildChatRequest(messages,conversationId);
        try {

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if(Objects.nonNull(customerCallBack)){
                        customerCallBack.onFailure();
                    }
                    if(Objects.nonNull(sysCallBack)){
                        sysCallBack.onFailure();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        if(Objects.nonNull(customerCallBack)){
                            customerCallBack.onFailure();
                        }
                        if(Objects.nonNull(sysCallBack)){
                            sysCallBack.onFailure();
                        }
                        return;
                    }


                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            BufferedSource source = responseBody.source();

                            while (!source.exhausted()) {
                                String line = source.readUtf8Line();

                                if (line != null) {
                                    if (line.trim().isEmpty() || line.startsWith("event:")) {
                                        continue;
                                    }
                                    if (line.startsWith("data: ")) {
                                        String data = line.substring(6);

                                        if ("[DONE]".equals(data.trim())) {
                                            if(Objects.nonNull(customerCallBack)){
                                                customerCallBack.finish();
                                            }
                                            if(Objects.nonNull(sysCallBack)){
                                                sysCallBack.finish();
                                            }
                                            break;
                                        }
                                        if(Objects.nonNull(customerCallBack)){
                                            customerCallBack.send(data);
                                        }
                                        if(Objects.nonNull(sysCallBack)){
                                            sysCallBack.send(data);
                                        }

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        if(Objects.nonNull(customerCallBack)){
                            customerCallBack.onFailure();
                        }
                        if(Objects.nonNull(sysCallBack)){
                            sysCallBack.onFailure();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}