package io.github.johntortoise.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.ImportConversationMessageDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.listner.ExcelImportListener;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.model.TortoiseMessage;
import io.github.johntortoise.service.TortoiseChatProfileService;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseMessageService;
import io.github.johntortoise.service.TransactionalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionalServiceImpl implements TransactionalService {

    @Resource
    @Lazy
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Resource
    @Lazy
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    @Lazy
    private TortoiseMessageService tortoiseMessageService;

    @Override
    public void importConversationFile(File file) {
        try( FileInputStream fis = new FileInputStream(file)) {
            ExcelImportListener listener = new ExcelImportListener();
            EasyExcel.read(fis, ImportConversationMessageDTO.class, listener)
                    .sheet(0)
                    .doRead();
        } catch (Exception e) {
            LogUtil.error("解析Excel文件失败:",e);
            throw new RuntimeException("解析Excel文件失败: " + e.getMessage(), e);
        }
    }




    
    @Transactional
    public void processBatchInTransaction(List<ImportConversationMessageDTO> batchData) {
        HashSet<String> uniqueIdSet = new HashSet<>();
        HashMap<String,Long> conversationIdToChatProfileIdListMap = new HashMap<>();
        for(ImportConversationMessageDTO importConversationMessageDTO:batchData){
            validData(importConversationMessageDTO);
            if(uniqueIdSet.contains(importConversationMessageDTO.getUniqueId())){
                throw new RuntimeException("出现重复的uniqueId"+importConversationMessageDTO.getUniqueId());
            }
            uniqueIdSet.add(importConversationMessageDTO.getUniqueId());
            conversationIdToChatProfileIdListMap.put(importConversationMessageDTO.getConversationId(),importConversationMessageDTO.getChatProfileId());
        }

        validChatProfileIds(new HashSet<>(conversationIdToChatProfileIdListMap.values()));

        autoCreateConversationIfNoExist(conversationIdToChatProfileIdListMap);

        batchInsertMessage(batchData,uniqueIdSet);
    }


    public void validChatProfileIds(Set<Long> chatProfileIdSet){
        List<TortoiseChatProfile> tortoiseChatProfilesDB = tortoiseChatProfileService.listByIds(chatProfileIdSet);
        if(EmptyUtil.isNotEmpty(tortoiseChatProfilesDB)){
            if(tortoiseChatProfilesDB.size()!=chatProfileIdSet.size()){
                Set<Long> chatProfileIdSetDB = tortoiseChatProfilesDB.stream().map(TortoiseChatProfile::getId).collect(Collectors.toSet());
                Set<Long> chatProfileIdNoExistSet = chatProfileIdSet.stream().filter(id -> !chatProfileIdSetDB.contains(id)).collect(Collectors.toSet());
                if(!chatProfileIdNoExistSet.isEmpty()){
                    throw new RuntimeException("chatProfileId不存在"+chatProfileIdNoExistSet);
                }
            }
        }else {
            throw new RuntimeException("chatProfileId不存在"+chatProfileIdSet);
        }
    }

    public void autoCreateConversationIfNoExist(HashMap<String,Long> conversationIdToChatProfileIdListMap){
        Set<String> conversationIdSet = conversationIdToChatProfileIdListMap.keySet();

        List<String> conversationIds = new ArrayList<>(conversationIdSet);

        
        Map<String, TortoiseConversation> existConversations = tortoiseConversationService
                .list(new QueryWrapper<TortoiseConversation>().lambda()
                        .in(TortoiseConversation::getConversationId, conversationIds))
                .stream()
                .collect(Collectors.toMap(
                        TortoiseConversation::getConversationId,
                        Function.identity(),
                        (a, b) -> a 
                ));

        List<TortoiseConversation> saveList = new ArrayList<>(conversationIds.size());

        for(String conversationId : conversationIds){
            
            if(!existConversations.containsKey(conversationId)){
                TortoiseConversation conversation = new TortoiseConversation();
                conversation.setChatProfileId(conversationIdToChatProfileIdListMap.get(conversationId));
                conversation.setCreateUserId(null);
                conversation.setIsDeleted(DeletedEnum.EXIST.getCode());
                conversation.setConversationId(conversationId);
                saveList.add(conversation);
            }
        }

        if(!saveList.isEmpty()){
            
            tortoiseConversationService.saveBatch(saveList, saveList.size());
        }
    }

    public void batchInsertMessage(List<ImportConversationMessageDTO> importConversationMessageDTOS, HashSet<String> uniqueIdSet){
        List<TortoiseMessage> tortoiseMessageDB = tortoiseMessageService.findByUniqueIdList(uniqueIdSet);
        if(EmptyUtil.isNotEmpty(tortoiseMessageDB)){
            Set<String> uniqueIdExistSet = tortoiseMessageDB.stream().map(TortoiseMessage::getUniqueId).collect(Collectors.toSet());
            throw new RuntimeException("唯一id已存在:"+uniqueIdExistSet);
        }
        List<TortoiseMessage> saveList = new ArrayList<>();
        for(ImportConversationMessageDTO dto:importConversationMessageDTOS){
            TortoiseMessage tortoiseMessage = new TortoiseMessage();
            tortoiseMessage.setConversationId(dto.getConversationId());
            tortoiseMessage.setUniqueId(dto.getUniqueId());
            tortoiseMessage.setInputContent(dto.getInputContent());
            tortoiseMessage.setInputRole(dto.getInputRole());
            tortoiseMessage.setOutputContent(dto.getOutputContent());
            tortoiseMessage.setOutputRole(dto.getOutputRole());
            saveList.add(tortoiseMessage);
        }
        tortoiseMessageService.saveBatch(saveList, saveList.size());
    }


    public void validData(ImportConversationMessageDTO importConversationMessageDTO){
        String conversationId = importConversationMessageDTO.getConversationId();
        String uniqueId = importConversationMessageDTO.getUniqueId();
        Long chatProfileId = importConversationMessageDTO.getChatProfileId();
        String inputContent = importConversationMessageDTO.getInputContent();
        String inputRole = importConversationMessageDTO.getInputRole();
        String outputRole = importConversationMessageDTO.getOutputRole();
        String outputContent = importConversationMessageDTO.getOutputContent();

        List<String> errorMessages = new ArrayList<>();

        if (EmptyUtil.isEmpty(conversationId)) {
            errorMessages.add("conversationId 不能为空");
        }
        if (EmptyUtil.isEmpty(uniqueId)) {
            errorMessages.add("uniqueId 不能为空");
        }
        if (EmptyUtil.isEmpty(chatProfileId)) {
            errorMessages.add("chatProfileId 不能为空");
        }
        if (EmptyUtil.isEmpty(inputRole)) {
            errorMessages.add("inputRole 不能为空");
        }
        if (EmptyUtil.isEmpty(outputRole)) {
            errorMessages.add("outputRole 不能为空");
        }
        if(EmptyUtil.isEmpty(inputContent) && EmptyUtil.isEmpty(outputContent)){
            errorMessages.add("inputContent 或 outputContent必须有值");
        }

        if (!errorMessages.isEmpty()) {
            throw new RuntimeException(String.join("; ", errorMessages));
        }

    }
}
