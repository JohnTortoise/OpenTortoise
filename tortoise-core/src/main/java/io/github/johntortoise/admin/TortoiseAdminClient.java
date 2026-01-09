package io.github.johntortoise.admin;

import io.github.johntortoise.core.consts.HTTPConst;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.model.Tool;
import io.github.johntortoise.core.dto.sys.*;
import io.github.johntortoise.core.utils.BaseHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TortoiseAdminClient extends BaseHttpClient {

    private final String url;

    private final String sk;


    public TortoiseAdminClient(String host, String sk) {
        super(createClient());
        this.sk = sk;
        this.url = host+"/api/admin/";
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
        executeGetReq(this.url+"connect",null,String.class);
    }


    public String createConversation(String customerId)  {
        HashMap<String,String> map = newMap();
        map.put("customerId",customerId);
        return executeGetReq(this.url+"createConversation",map,String.class);
    }

    public Model findModel(String conversationId){
        return executeGetReq(this.url+"getModelByConversationId",newMap(conversationId),Model.class);
    }


    public void afterChat(AfterChatDTO afterChatDTO) {
        executePostReq(this.url+"afterChat", afterChatDTO,String.class);
    }


    public List<Message> getMemoriesByConversationId(String conversationId) {
        GetMemoriesByConversationIdResp getMemoriesByConversationIdResp = executeGetReq(this.url + "getMemoriesByConversationId", newMap(conversationId), GetMemoriesByConversationIdResp.class);
        return getMemoriesByConversationIdResp.getMessages();
    }

    public Boolean checkLimit(String conversationId){
        return executeGetReq(this.url+"checkLimit",newMap(conversationId),Boolean.class);
    }

    public void receiveMessage(ReceiveMessageReq req){
        executePostReq(this.url+"receiveMessage",req,String.class);
    }

    public List<Tool> batchGetToolByNames(List<String> toolNames){
        BatchGetToolByNamesReq batchGetToolByNamesReq = BatchGetToolByNamesReq.builder().toolsNameList(toolNames).build();
        BatchGetToolByNamesResp batchGetToolByNamesResp = executePostReq(this.url + "batchGetToolByNames", batchGetToolByNamesReq, BatchGetToolByNamesResp.class);
        return batchGetToolByNamesResp.getTools();
    }

    public String callTool(String name,String args){
        HashMap<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("args",args);
        return executeGetReq(this.url+"callTool",map,String.class);
    }




    private  <T> T executeGetReq(String url, Map<String,String> map,Class<T> responseType){
        return doGet(url,map,generateHeaders(),responseType);
    }

    private <T> T executePostReq(String url, Object params,Class<T> responseType){
        return doPost(url,params,generateHeaders(),responseType);
    }

    public Map<String,String> generateHeaders(){
        Map<String, String> headerMap = newMap();
        headerMap.put(HTTPConst.X_API_KEY,this.sk);
        return headerMap;
    }

    public HashMap<String,String> newMap(){
        return new HashMap<>();
    }

    public HashMap<String,String> newMap(String conversationId){
        HashMap<String,String> map = newMap();
        map.put("conversationId",conversationId);
        return map;
    }



}