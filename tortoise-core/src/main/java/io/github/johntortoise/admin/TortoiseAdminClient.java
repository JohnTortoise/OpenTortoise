package io.github.johntortoise.admin;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.ReceiveMessageReq;
import io.github.johntortoise.core.utils.BaseHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TortoiseAdminClient extends BaseHttpClient {

    private final String adminPath = "/api/admin/";

    public TortoiseAdminClient(String host, String sk) {
        super(createClient(), host,sk);
    }

    private static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 10, TimeUnit.MINUTES))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }


    public void connect()  {
        HttpUrl.Builder urlBuilder = buildUrl(adminPath+"connect");
        doGet("", urlBuilder, Boolean.class);
    }


    public String createConversation(String customerId)  {
        HttpUrl.Builder urlBuilder = buildUrl(adminPath+"createConversation")
                .addQueryParameter("customerId", customerId);
        return doGet("", urlBuilder, String.class);
    }



    public Model findModel(String conversationId){
        HttpUrl.Builder urlBuilder = buildUrl(adminPath+"getModelByConversationId")
                .addQueryParameter("conversationId", conversationId);
        
        return doGet("", urlBuilder, Model.class);
    }


    public void afterChat(AfterChatDTO afterChatDTO) {
        doPost(adminPath+"afterChat", afterChatDTO);
    }


    public List<Message> getMemoriesByConversationId(String conversationId) {
        HttpUrl.Builder urlBuilder = buildUrl(adminPath+"getMemoriesByConversationId")
                .addQueryParameter("conversationId", conversationId);
        
        return doGet("", urlBuilder, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class));
    }

    public Boolean checkLimit(String conversationId){
        HttpUrl.Builder urlBuilder = buildUrl(adminPath+"checkLimit")
                .addQueryParameter("conversationId", conversationId);
        return doGet("", urlBuilder, Boolean.class);
    }

    public void receiveMessage(ReceiveMessageReq req){
        doPost(adminPath+"receiveMessage",req);
    }
}