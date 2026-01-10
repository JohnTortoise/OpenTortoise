package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.generator.UniqueIdGenerator;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.mapper.TortoiseConversationMapper;
import io.github.johntortoise.model.*;
import io.github.johntortoise.service.*;
import io.github.johntortoise.utils.PageConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TortoiseConversationServiceImpl extends ServiceImpl<TortoiseConversationMapper, TortoiseConversation> implements TortoiseConversationService {

    @Resource
    @Lazy
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Resource
    private TransactionalService transactionalService;

    @Resource
    private TortoiseImportFileRecordService tortoiseImportFileRecordService;

    @Resource
    private TortoiseLlmConfigService tortoiseLlmConfigService;

    @Resource
    private TortoiseMemoryPolicyService memoryPolicyService;
    @Autowired
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;

    @Resource
    private TortoiseMessageService tortoiseMessageService;

    @Resource
    private TortoiseMessageExtendService tortoiseMessageExtendService;


    @Override
    public Page<TortoiseConversationDTO> page(String conversationId, Long userId,
                                              Long current, Long size) {
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        
        LambdaQueryWrapper<TortoiseConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseConversation::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseConversation::getUpdateTime);
        if (Objects.nonNull(userId)) {
            queryWrapper.eq(TortoiseConversation::getCreateUserId, userId);
        }
        if (EmptyUtil.isNotEmpty(conversationId)) {
            queryWrapper.eq(TortoiseConversation::getConversationId, conversationId);
        }
        Page<TortoiseConversation> page = this.baseMapper.selectPage(new Page<>(current, size), queryWrapper);

        if (page.getRecords().isEmpty()) {
            return PageConvertUtil.convert(page, conversation -> null);
        }


        Set<Long> profileIdSet = page.getRecords().stream()
                .map(TortoiseConversation::getChatProfileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (profileIdSet.isEmpty()) {
            LogUtil.warn("对话记录中没有有效的配置ID");
            return PageConvertUtil.convert(page, conversation -> null);
        }
        
        List<TortoiseChatProfile> list = tortoiseChatProfileService.listByIds(profileIdSet);

        Map<Long,String> llmConfigIdToNameMap = new HashMap<>();
        Map<Long,String> memoryConfigIdToNameMap = new HashMap<>();
        Map<Long, TortoiseChatProfile> chatProfileMap = new HashMap<>();

        if(EmptyUtil.isNotEmpty(list)){
            chatProfileMap = list.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(TortoiseChatProfile::getId, Function.identity()));

            Set<Long> llmConfigIdSet = list.stream().map(TortoiseChatProfile::getLlmConfigId).collect(Collectors.toSet());
            if(EmptyUtil.isNotEmpty(llmConfigIdSet)){
                List<TortoiseLlmConfig> tortoiseLlmConfigs = tortoiseLlmConfigService.listByIds(llmConfigIdSet);
                llmConfigIdToNameMap = tortoiseLlmConfigs.stream().collect(Collectors.toMap(TortoiseLlmConfig::getId,TortoiseLlmConfig::getConfigName));
            }
            Set<Long> memoryPolicyIdSet = list.stream().map(TortoiseChatProfile::getMemoryPolicyId).collect(Collectors.toSet());
            if(EmptyUtil.isNotEmpty(memoryPolicyIdSet)){
                List<TortoiseMemoryPolicy> tortoiseMemoryPolicies = tortoiseMemoryPolicyService.listByIds(memoryPolicyIdSet);
                memoryConfigIdToNameMap = tortoiseMemoryPolicies.stream().collect(Collectors.toMap(TortoiseMemoryPolicy::getId,TortoiseMemoryPolicy::getName));
            }
        }

        Map<Long, TortoiseChatProfile> finalChatProfileMap = chatProfileMap;
        Map<Long, String> finalLlmConfigIdToNameMap = llmConfigIdToNameMap;
        Map<Long, String> finalMemoryConfigIdToNameMap = memoryConfigIdToNameMap;

        return PageConvertUtil.convert(page, conversation ->
                TortoiseConversation.covertToDTO(conversation, finalChatProfileMap, finalLlmConfigIdToNameMap, finalMemoryConfigIdToNameMap));
    }



    @Override
    public TortoiseConversation createConversation(Long userId, Long chatProfileId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (chatProfileId == null) {
            throw new IllegalArgumentException("聊天配置ID不能为空");
        }
        
        try {
            TortoiseConversation conversation = new TortoiseConversation();
            conversation.setChatProfileId(chatProfileId);
            conversation.setCreateUserId(userId);
            conversation.setIsDeleted(DeletedEnum.EXIST.getCode());
            conversation.setConversationId(UniqueIdGenerator.generateId());
            this.save(conversation);
            LogUtil.info("创建对话成功: conversationId={}, userId={}, chatProfileId={}",
                    conversation.getConversationId(), userId, chatProfileId);
            return conversation;
        } catch (Exception e) {
            LogUtil.error("创建对话失败: userId={}, chatProfileId={}", userId, chatProfileId, e);
            throw e;
        }
    }


    @Override
    public TortoiseConversationDTO getByConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话ID不能为空");
        }
        
        TortoiseConversation conversation = this.getOne(new QueryWrapper<TortoiseConversation>()
                .lambda().eq(TortoiseConversation::getConversationId, conversationId));
        
        if (conversation == null) {
            throw new io.github.johntortoise.core.exceptions.TortoiseBusinessException(
                    io.github.johntortoise.core.enums.ErrorCodeEnum.BUSINESS_ERROR, "对话不存在");
        }
        
        if (conversation.getChatProfileId() == null) {
            throw new io.github.johntortoise.core.exceptions.TortoiseBusinessException(
                    io.github.johntortoise.core.enums.ErrorCodeEnum.BUSINESS_ERROR, "对话配置ID为空");
        }
        
        TortoiseChatProfile chatProfile = tortoiseChatProfileService.getById(conversation.getChatProfileId());
        if (chatProfile == null) {
            throw new io.github.johntortoise.core.exceptions.TortoiseBusinessException(
                    io.github.johntortoise.core.enums.ErrorCodeEnum.BUSINESS_ERROR, "对话配置不存在");
        }
        
        HashMap<Long, TortoiseChatProfile> map = new HashMap<>();
        map.put(conversation.getChatProfileId(), chatProfile);

        Long llmConfigId = chatProfile.getLlmConfigId();
        Long memoryPolicyId = chatProfile.getMemoryPolicyId();

        TortoiseLlmConfig tortoiseLlmConfig = tortoiseLlmConfigService.getById(llmConfigId);
        TortoiseMemoryPolicy tortoiseMemoryPolicy = tortoiseMemoryPolicyService.getById(memoryPolicyId);

        HashMap<Long,String> llmConfigIdToNameMap = new HashMap<>();
        llmConfigIdToNameMap.put(llmConfigId,tortoiseLlmConfig.getConfigName());

        Map<Long,String> memoryConfigIdToNameMap = new HashMap<>();
        memoryConfigIdToNameMap.put(memoryPolicyId,tortoiseMemoryPolicy.getName());

        return TortoiseConversation.covertToDTO(conversation, map,llmConfigIdToNameMap,memoryConfigIdToNameMap);
    }

    @Override
    public TortoiseConversation getMinIdByChatProfileId(Long chatProfileId) {
        if (chatProfileId == null) {
            throw new IllegalArgumentException("聊天配置ID不能为空");
        }
        
        return this.getOne(new LambdaQueryWrapper<TortoiseConversation>()
                .eq(TortoiseConversation::getChatProfileId, chatProfileId)
                .orderByAsc(TortoiseConversation::getId)
                .last("limit 1"));
    }

    @Override
    public TortoiseConversation getMaxIdByChatProfileId(Long chatProfileId) {
        if (chatProfileId == null) {
            throw new IllegalArgumentException("聊天配置ID不能为空");
        }
        
        return this.getOne(new LambdaQueryWrapper<TortoiseConversation>()
                .eq(TortoiseConversation::getChatProfileId, chatProfileId)
                .orderByDesc(TortoiseConversation::getId)
                .last("limit 1"));
    }

    @Override
    public List<TortoiseConversation> listByChatProfileIdAndIdRange(Long chatProfileId, Long minId, Long maxId) {
        if (chatProfileId == null) {
            throw new IllegalArgumentException("聊天配置ID不能为空");
        }
        if (minId == null || maxId == null) {
            throw new IllegalArgumentException("ID范围不能为空");
        }
        
        return this.list(new LambdaQueryWrapper<TortoiseConversation>()
                .eq(TortoiseConversation::getChatProfileId, chatProfileId)
                .ge(TortoiseConversation::getId, minId)
                .le(TortoiseConversation::getId, maxId));
    }

    @Override
    @Async(value = "importConversationThreadPool")
    public void importExcel(Long recordId,String tempFilePath, String originalFilename) {
        File tempFile = new File(tempFilePath);
        try {
            LogUtil.info("开始异步处理Excel文件: {}, 临时文件路径: {}", originalFilename, tempFilePath);
            transactionalService.importConversationFile(tempFile);
            LogUtil.info("Excel文件处理完成: {}", originalFilename);
            tortoiseImportFileRecordService.success(recordId);
        } catch (Exception e) {
            tortoiseImportFileRecordService.fail(recordId,e.getMessage());
            LogUtil.error("处理Excel文件失败: {}", originalFilename, e);
        } finally {
            try {
                if (tempFile.exists()) {
                    Files.delete(Paths.get(tempFilePath));
                    LogUtil.info("临时文件已删除: {}", tempFilePath);
                }
            } catch (IOException e) {
                LogUtil.error("删除临时文件失败: {}", tempFilePath, e);
            }
        }
    }

    @Override
    public String copyConversation(String conversationId) {
        TortoiseConversationDTO tortoiseConversationDTO = this.getByConversationId(conversationId);
        Long currentUserId = TortoiseContext.getCurrentUserId();
        TortoiseConversation conversation = createConversation(tortoiseConversationDTO.getTortoiseChatProfileId(), currentUserId);


        Long pageNum = 1L;
        while (true){
            Page<TortoiseMessage> tortoiseMessagePage =
                    tortoiseMessageService.queryPageByConversationId(conversationId, pageNum,
                    100L,true);
            if(EmptyUtil.isEmpty(tortoiseMessagePage.getRecords())){
                break;
            }
            List<Long> messageIdList =
                    tortoiseMessagePage.getRecords().stream().map(TortoiseMessage::getId).collect(Collectors.toList());
            List<TortoiseMessageExtend> tortoiseMessageExtends = tortoiseMessageExtendService.getByMessageIdList(messageIdList);
            Map<Long, TortoiseMessageExtend> messageIdToExtentMap =
                    tortoiseMessageExtends.stream().collect(Collectors.toMap(TortoiseMessageExtend::getMessageId,
                            Function.identity()));
            Map<Long,TortoiseMessage> map = new HashMap<>();
            for(TortoiseMessage tortoiseMessage:tortoiseMessagePage.getRecords()){
                map.put(tortoiseMessage.getId(),tortoiseMessage);
                tortoiseMessage.setId(null);
                tortoiseMessage.setConversationId(conversation.getConversationId());
                tortoiseMessage.setUniqueId(UniqueIdGenerator.generateId());
            }
            tortoiseMessageService.saveBatch(tortoiseMessagePage.getRecords());

            List<TortoiseMessageExtend> saveExtendList = new ArrayList<>();

            map.forEach((messageId,tortoiseMessage)->{
                TortoiseMessageExtend tortoiseMessageExtend = messageIdToExtentMap.getOrDefault(messageId, null);
                if(Objects.nonNull(tortoiseMessageExtend)){
                    tortoiseMessageExtend.setMessageId(tortoiseMessage.getId());
                    tortoiseMessageExtend.setId(null);
                    saveExtendList.add(tortoiseMessageExtend);
                }
            });

            if(EmptyUtil.isNotEmpty(saveExtendList)){
                tortoiseMessageExtendService.saveBatch(saveExtendList);
            }

            pageNum++;
        }
        return conversation.getConversationId();
    }


}