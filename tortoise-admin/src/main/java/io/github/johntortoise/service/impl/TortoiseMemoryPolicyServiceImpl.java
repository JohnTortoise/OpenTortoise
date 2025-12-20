package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.client.llm.CompleteLLMApiClient;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Model;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.message.impl.DefaultTortoiseMessageHandler;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.MessageUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.context.TortoiseContext;
import io.github.johntortoise.dto.AnalysisMessageForMemoryDTO;
import io.github.johntortoise.dto.MemoryBase;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDTO;
import io.github.johntortoise.dto.TortoiseMemoryPolicyDetailDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.MemoryTypeEnum;
import io.github.johntortoise.enums.TortoiseLlmUsageEnum;
import io.github.johntortoise.mapper.TortoiseMemoryPolicyMapper;
import io.github.johntortoise.model.*;
import io.github.johntortoise.service.*;
import io.github.johntortoise.utils.PageConvertUtil;
import io.github.johntortoise.utils.SimpleLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class TortoiseMemoryPolicyServiceImpl extends ServiceImpl<TortoiseMemoryPolicyMapper, TortoiseMemoryPolicy> implements TortoiseMemoryPolicyService {

    @Resource
    @Lazy
    private TortoiseMemoryService tortoiseMemoryService;

    @Autowired
    private TortoiseMessageService tortoiseMessageService;


    @Resource
    private TortoiseLlmConfigService llmConfigService;

    @Resource
    private TortoiseLlmUsageService tortoiseLlmUsageService;

    @Resource
    private TortoiseMemoryPromptConfigService tortoiseMemoryPromptConfigService;

    @Resource
    @Lazy
    private TortoiseChatProfileService tortoiseChatProfileService;


    @Override
    public Page<TortoiseMemoryPolicyDTO> page(String name, Long current, Long size) {
        LambdaQueryWrapper<TortoiseMemoryPolicy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseMemoryPolicy::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseMemoryPolicy::getUpdateTime);
        if(EmptyUtil.isNotEmpty(name)){
            queryWrapper.like(TortoiseMemoryPolicy::getName,name);
        }
        Page<TortoiseMemoryPolicy> page = this.baseMapper.selectPage(new Page<>(current, size), queryWrapper);

        Map<Long, String> modelIdToModelNameMap = new HashMap<>();
        if(EmptyUtil.isNotEmpty(page.getRecords())){
            Set<Long> configId = page.getRecords().stream().map(TortoiseMemoryPolicy::getLlmConfigId).collect(Collectors.toSet());
            List<TortoiseLlmConfig> configs = llmConfigService.listByIds(configId);
            modelIdToModelNameMap = configs.stream().collect(Collectors.toMap(TortoiseLlmConfig::getId,
                    TortoiseLlmConfig::getModelName));
        }

        Map<Long, String> finalModelIdToModelNameMap = modelIdToModelNameMap;
        return PageConvertUtil.convert(page,
                usage -> TortoiseMemoryPolicy.covertToDTO(usage, finalModelIdToModelNameMap));
    }


    @Override
    public TortoiseMemoryPolicyDetailDTO detail(Long id) {
        try {
            ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

            TortoiseMemoryPolicy tortoiseMemoryPolicy = this.getById(id);
            TortoiseLlmConfig tortoiseLlmConfig = llmConfigService.getById(tortoiseMemoryPolicy.getLlmConfigId());

            MemoryBase memoryBase = objectMapper.readValue(tortoiseMemoryPolicy.getConfig(), MemoryBase.class);


            return TortoiseMemoryPolicyDetailDTO.builder()
                    .llmModelConfigId(tortoiseMemoryPolicy.getLlmConfigId())
                    .modelName(Objects.nonNull(tortoiseLlmConfig)?tortoiseLlmConfig.getModelName():"")
                    .name(tortoiseMemoryPolicy.getName())
                    .id(tortoiseMemoryPolicy.getId())
                    .memoryBase(memoryBase)
                    .build();
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }

    @Override
    public MemoryBase autoFixIfPromptIsEmpty(MemoryBase memoryBase){
        if(EmptyUtil.isEmpty(memoryBase.getBasePrompt()) || EmptyUtil.isEmpty(memoryBase.getUpdatePrompt()) || EmptyUtil.isEmpty(memoryBase.getOverLengthPrompt())){
            TortoiseMemoryPromptConfig prompt = tortoiseMemoryPromptConfigService.findPrompt(memoryBase.getType());
            if(Objects.nonNull(prompt)){
                if(EmptyUtil.isEmpty(memoryBase.getBasePrompt())){
                    memoryBase.setBasePrompt(prompt.getBasePrompt());
                }
                if(EmptyUtil.isEmpty(memoryBase.getUpdatePrompt())){
                    memoryBase.setUpdatePrompt(prompt.getUpdatePrompt());
                }
                if(EmptyUtil.isEmpty(memoryBase.getOverLengthPrompt())){
                    memoryBase.setOverLengthPrompt(prompt.getOverLengthPrompt());
                }
            }
        }
        return memoryBase;
    }


    @Override
    public void addOrUpdate(TortoiseMemoryPolicyDetailDTO tortoiseMemoryPolicyDetailDTO) {
        try {
            validParams(tortoiseMemoryPolicyDetailDTO);
            ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
            
            String configJson = objectMapper.writeValueAsString(tortoiseMemoryPolicyDetailDTO.getMemoryBase());

            if(Objects.nonNull(tortoiseMemoryPolicyDetailDTO.getId())){
                UpdateWrapper<TortoiseMemoryPolicy> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", tortoiseMemoryPolicyDetailDTO.getId())
                        .set("name", tortoiseMemoryPolicyDetailDTO.getName())
                        .set("llm_config_id", tortoiseMemoryPolicyDetailDTO.getLlmModelConfigId())
                        .set("config", configJson);
                this.update(updateWrapper);
            }else {
                TortoiseMemoryPolicy tortoiseMemoryPolicy = TortoiseMemoryPolicy.builder()
                        .id(tortoiseMemoryPolicyDetailDTO.getId())
                        .name(tortoiseMemoryPolicyDetailDTO.getName())
                        .llmConfigId(tortoiseMemoryPolicyDetailDTO.getLlmModelConfigId())
                        .config(configJson)
                        .isDeleted(DeletedEnum.EXIST.getCode())
                        .createUserId(TortoiseContext.getCurrentUserId())
                        .build();
                this.save(tortoiseMemoryPolicy);
            }
        } catch (Exception e) {
            LogUtil.error("error",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e.getMessage(), e);
        }
    }

    public void validParams(TortoiseMemoryPolicyDetailDTO tortoiseMemoryPolicyDetailDTO){

        if(EmptyUtil.isEmpty(tortoiseMemoryPolicyDetailDTO.getName())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"记忆策略名称不能为空");
        }
        MemoryBase memoryBase = tortoiseMemoryPolicyDetailDTO.getMemoryBase();
        if(EmptyUtil.isEmpty(memoryBase)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"记忆基础配置为空");
        }
        if(EmptyUtil.isEmpty(memoryBase.getThreshold())){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"记忆阈值必须填入");
        }
        if(memoryBase.getType()!=1L){
            Long llmModelConfigId = tortoiseMemoryPolicyDetailDTO.getLlmModelConfigId();
            if(Objects.isNull(llmModelConfigId) && !memoryBase.getType().equals(MemoryTypeEnum.RECENT.getCode())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"只有截断类型才可以不选择大模型");
            }
            if(EmptyUtil.isEmpty(memoryBase.getBatchAnalysisSize())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"记忆单次分析消息数量不能为空");
            }
            if(EmptyUtil.isEmpty(memoryBase.getMaxLength())){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"记忆最大长度不能为空");
            }
        }
    }




    @Override
    public void memoryReconstructionByPolicy(String conversationId,Long memoryPolicyId){
        try {
            TortoiseMemoryPolicy tortoiseMemoryPolicy = this.getById(memoryPolicyId);
            MemoryBase memoryBase = ObjectMapperUtil.createObjectMapper().readValue(tortoiseMemoryPolicy.getConfig(), MemoryBase.class);
            if(memoryBase.getType().equals(MemoryTypeEnum.RECENT.getCode())){
                return;
            }
            memoryReconstructionByStrategy(conversationId,tortoiseMemoryPolicy.getConfig(),tortoiseMemoryPolicy.getLlmConfigId());
        }catch (Exception e){
            LogUtil.error("memoryReconstructionByPolicy error",e);
            throw new RuntimeException(e);
        }
    }



    public void memoryReconstructionByStrategy(String conversationId, String config, Long llmConfigId) {
        String key = conversationId+"-memoryReconstructionByStrategy";
        boolean lockFlag = false;
        try {
            if(SimpleLock.tryLock(key,600)){
                lockFlag =true;
                MemoryReconstructionContext context = initializeContext(conversationId, config, llmConfigId);
                if (shouldProcessMemory(context)) {
                    LogUtil.info("conversationId:{} 触发记忆",conversationId);
                    
                    processMemoryAnalysis(context);
                    
                    processHistoricalMemoryIfNeeded(context);
                    
                    processCondensedMemoryIfNeeded(context);
                    
                    saveMemory(context);
                    LogUtil.info("conversationId:{} 记忆完成",conversationId);
                }
            }else {
                LogUtil.warn(key+"拿不到锁，跳过");
            }
        } catch (Exception e) {
            LogUtil.error("memoryReconstructionByStrategy error",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e);
        }finally {
            if(lockFlag){
                SimpleLock.unlock(key);
            }
        }
    }

    private MemoryReconstructionContext initializeContext(String conversationId, String config, Long llmConfigId)
            throws JsonProcessingException {
        MemoryReconstructionContext context = new MemoryReconstructionContext();
        context.conversationId = conversationId;
        context.llmConfigId = llmConfigId;

        ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();
        context.objectMapper = objectMapper;

        context.memoryBase = objectMapper.readValue(config, MemoryBase.class);

        context.memoryBase = this.autoFixIfPromptIsEmpty(context.memoryBase);
        TortoiseLlmConfig tortoiseLlmConfig = llmConfigService.getById(llmConfigId);
        context.llmConfig = tortoiseLlmConfig;
        context.llmClient = CompleteLLMApiClient.createClient(new Model(tortoiseLlmConfig.getApiUrl(),tortoiseLlmConfig.getApiKey(),tortoiseLlmConfig.getModelName(),tortoiseLlmConfig.getTemperature()));
        return context;
    }


    private boolean shouldProcessMemory(MemoryReconstructionContext context) {
        Long lastMessageId = findLastMessageId(context.conversationId);
        context.existingMemory = findExistingMemory(context.conversationId);

        if (lastMessageId != null && lastMessageId > 0) {
            return shouldProcessWithExistingMemory(context, lastMessageId);
        } else {
            return shouldProcessWithoutExistingMemory(context);
        }
    }

    private Long findLastMessageId(String conversationId) {
        TortoiseMemory memory = tortoiseMemoryService.findMemoryByConversation(conversationId);
        return memory != null ? memory.getLastMessageId() : null;
    }

    private TortoiseMemory findExistingMemory(String conversationId) {
        return tortoiseMemoryService.findMemoryByConversation(conversationId);
    }

    private boolean shouldProcessWithExistingMemory(MemoryReconstructionContext context, Long lastMessageId) {
        long newMessageCount = countMessagesSinceLast(context.conversationId, lastMessageId,
                context.memoryBase.getThreshold());

        LogUtil.info("conversationId:{} lastMessageId:{} newMessageCount:{}",context.conversationId,lastMessageId,newMessageCount);

        if (newMessageCount >= context.memoryBase.getThreshold()) {
            context.havingHistory = true;
            if (context.memoryBase.getWithHistory()) {
                MessageIdRange range = findMessageIdRange(context.conversationId, lastMessageId);
                context.thisAnalysisMinId = range.minId;
                context.thisAnalysisMaxId = range.maxId;
            }
            return true;
        }
        return false;
    }

    private boolean shouldProcessWithoutExistingMemory(MemoryReconstructionContext context) {
        long totalMessageCount = countAllMessages(context.conversationId);
        LogUtil.info("conversationId:{} totalMessageCount:{}",context.conversationId,totalMessageCount);
        
        if (totalMessageCount >= context.memoryBase.getThreshold()) {
            MessageIdRange range = findMessageIdRange(context.conversationId, null);
            context.thisAnalysisMinId = range.minId;
            context.thisAnalysisMaxId = range.maxId;
            return true;
        }
        return false;
    }

    private long countMessagesSinceLast(String conversationId, Long lastMessageId, Long threshold) {
        return tortoiseMessageService.count(new QueryWrapper<TortoiseMessage>()
                .lambda()
                .eq(TortoiseMessage::getConversationId, conversationId)
                .gt(TortoiseMessage::getId, lastMessageId)
                .last("limit " + threshold));
    }

    private long countAllMessages(String conversationId) {
        return tortoiseMessageService.count(new QueryWrapper<TortoiseMessage>()
                .lambda()
                .eq(TortoiseMessage::getConversationId, conversationId));
    }

    private MessageIdRange findMessageIdRange(String conversationId, Long lastMessageId) {
        MessageIdRange range = new MessageIdRange();

        TortoiseMessage tortoiseMessageMin = tortoiseMessageService.findGtLastMessageId(conversationId, lastMessageId, true);
        TortoiseMessage tortoiseMessageMax = tortoiseMessageService.findGtLastMessageId(conversationId, lastMessageId, false);

        if(Objects.nonNull(tortoiseMessageMin) && Objects.nonNull(tortoiseMessageMax)){
            range.minId = tortoiseMessageMin.getId();
            range.maxId = tortoiseMessageMax.getId();
        }
        return range;
    }

    private void processMemoryAnalysis(MemoryReconstructionContext context) {
        AnalysisMessageForMemoryDTO analysis = analysis(
                context.memoryBase.getBatchAnalysisSize(),
                context.conversationId,
                context.thisAnalysisMaxId,
                context.thisAnalysisMinId,
                context.llmClient,
                context.memoryBase.getBasePrompt(),
                context.memoryBase.getUpdatePrompt()
                );

        context.lastMessageId = analysis.getLastMessageId();
        context.content = analysis.getResult();
    }

    private void processHistoricalMemoryIfNeeded(MemoryReconstructionContext context) {
        if (context.havingHistory && context.memoryBase.getWithHistory()) {
            context.content = fusionMemory(
                    context.conversationId,
                    context.memoryBase.getUpdatePrompt(),
                    context.existingMemory.getContent(),
                    context.content,
                    context.llmClient,
                    context.objectMapper);
        }
    }

    private void processCondensedMemoryIfNeeded(MemoryReconstructionContext context) {
        context.content = condensedMemory(
                context.conversationId,
                context.memoryBase.getOverLengthPrompt(),
                context.content,
                context.llmClient,
                context.objectMapper,
                context.memoryBase.getMaxLength(),
                3);
    }

    private void saveMemory(MemoryReconstructionContext context) {
        TortoiseMemory tortoiseMemory = new TortoiseMemory();
        tortoiseMemory.setConversationId(context.conversationId);
        tortoiseMemory.setContent(context.content);
        tortoiseMemory.setLastMessageId(context.lastMessageId);
        tortoiseMemoryService.save(tortoiseMemory);
    }


    private static class MemoryReconstructionContext {
        String conversationId;
        Long llmConfigId;
        ObjectMapper objectMapper;
        MemoryBase memoryBase;
        TortoiseLlmConfig llmConfig;
        CompleteLLMApiClient llmClient;
        TortoiseMemory existingMemory;
        boolean havingHistory = false;
        Long thisAnalysisMinId;
        Long thisAnalysisMaxId;
        Long lastMessageId;
        String content;
    }

    private static class MessageIdRange {
        Long minId;
        Long maxId;
    }


    
    public String fusionMemory(String conversationId,String prompt,String history,String thisAnalysisResult,CompleteLLMApiClient completeLLMApiClient,ObjectMapper objectMapper){
        try {
            if(tortoiseChatProfileService.checkLimit(conversationId)){
                throw new RuntimeException(String.format("生成%s记忆时达到token限制",conversationId));
            }
            LogUtil.info("conversationId:{} 开始精简记忆",conversationId);
            List<Message> messages = MessageUtil.buildMessage(prompt,history,thisAnalysisResult);
            String reply = completeLLMApiClient.chatCompletion(messages, conversationId);
            ChatCompletionResponse chatCompletionResponse = objectMapper.readValue(reply, ChatCompletionResponse.class);

            recordUsage(messages,conversationId,reply, TortoiseLlmUsageEnum.MEMORY_UPDATE);

            return chatCompletionResponse.getChoices()[0].getMessage().getContent();
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }


    public String condensedMemory(String conversationId,String prompt,String content,
                                  CompleteLLMApiClient completeLLMApiClient,ObjectMapper objectMapper,Long maxLength, Integer retryCount){
        if(tortoiseChatProfileService.checkLimit(conversationId)){
            throw new RuntimeException(String.format("生成%s记忆时达到token限制",conversationId));
        }
        LogUtil.info("conversationId:{} 开始融合历史记忆和新记忆",conversationId);

        Integer count = 0;
        while (count<retryCount && content.length()>maxLength){
            try {
                List<Message> messages = MessageUtil.buildMessage(prompt+"\n注意，不得超过:"+maxLength+"个字\n",content);
                String reply = completeLLMApiClient.chatCompletion(messages, conversationId);
                ChatCompletionResponse chatCompletionResponse = objectMapper.readValue(reply, ChatCompletionResponse.class);
                recordUsage(messages,conversationId,reply, TortoiseLlmUsageEnum.MEMORY_OVER_LENGTH);
                content =  chatCompletionResponse.getChoices()[0].getMessage().getContent();
                count++;
            }catch (Exception e){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
            }
        }
        if(Objects.equals(count, retryCount) && content.length()>maxLength){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"超过最大限制长度且达到精简次数后仍超过最大精简次数");
        }
        return content;
    }



    public AnalysisMessageForMemoryDTO analysis(Long maxBatchSize, String conversationId, Long maxId, Long minId,
                                                CompleteLLMApiClient completeLLMApiClient, String basePrompt, String updatePrompt){

        ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

        List<String> result = new ArrayList<>();
        while (true){
            List<Message> oneBatchMessageList = new ArrayList<>();
            List<TortoiseMessage> list = tortoiseMessageService.list(new QueryWrapper<TortoiseMessage>().lambda()
                    .ge(TortoiseMessage::getId, minId)
                    .le(TortoiseMessage::getId,minId+maxBatchSize));
            if(EmptyUtil.isEmpty(list)){
                break;
            }
            list.forEach(tortoiseMessage -> {
                oneBatchMessageList.add(new Message(tortoiseMessage.getInputContent(),tortoiseMessage.getInputRole()));
                oneBatchMessageList.add(new Message(tortoiseMessage.getOutputContent(),tortoiseMessage.getOutputRole()));
            });
            result.add(analysisCore(conversationId,oneBatchMessageList,basePrompt,completeLLMApiClient,objectMapper));
            minId = Math.min(maxId,minId+maxBatchSize+1);
            if(Objects.equals(minId, maxId)){
                break;
            }
        }
        String content = "";
        if(result.size()>1){
            content = oversizeUpdate(conversationId,result,updatePrompt,completeLLMApiClient,objectMapper);
        }else {
            content = result.get(0);
        }
        return AnalysisMessageForMemoryDTO.builder().result(content).lastMessageId(minId).build();
    }

    public String oversizeUpdate(String conversationId,List<String> list,String updatePrompt,CompleteLLMApiClient completeLLMApiClient,ObjectMapper objectMapper){
        try {
            if(tortoiseChatProfileService.checkLimit(conversationId)){
                throw new RuntimeException(String.format("生成%s记忆时达到token限制",conversationId));
            }
            LogUtil.info("conversationId:{} 分批记忆更新",conversationId);

            String[] contentArray = new String[list.size()];
            for(int i=0;i<list.size();i++){
                contentArray[i] = list.get(i);
            }
            List<Message> messages = MessageUtil.buildMessage(updatePrompt, contentArray);
            String reply = completeLLMApiClient.chatCompletion(messages, conversationId);

            ChatCompletionResponse chatCompletionResponse = objectMapper.readValue(reply, ChatCompletionResponse.class);

            recordUsage(messages,conversationId,reply, TortoiseLlmUsageEnum.MEMORY_UPDATE);

            return chatCompletionResponse.getChoices()[0].getMessage().getContent();
        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }

    public String analysisCore(String conversationId,List<Message> list,String prompt,
                               CompleteLLMApiClient completeLLMApiClient,ObjectMapper objectMapper){
        try {
            if(tortoiseChatProfileService.checkLimit(conversationId)){
                throw new RuntimeException(String.format("生成%s记忆时达到token限制",conversationId));
            }

            LogUtil.info("conversationId:{} 开始为内容:{} 生成记忆",conversationId,list);
            StringBuilder allMessage = new StringBuilder();
            for(Message message:list){
                allMessage.append(message.getRole()).append(":").append(message.getContent()).append("\n");
            }

            List<Message> messages = MessageUtil.buildMessage(prompt,allMessage.toString());

            String reply = completeLLMApiClient.chatCompletion(messages, conversationId);

            ChatCompletionResponse chatCompletionResponse = objectMapper.readValue(reply, ChatCompletionResponse.class);

            recordUsage(messages,conversationId,reply, TortoiseLlmUsageEnum.MEMORY_BASE);

            return chatCompletionResponse.getChoices()[0].getMessage().getContent();

        }catch (Exception e){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }


    public void recordUsage(List<Message> messages, String conversationId, String reply, TortoiseLlmUsageEnum tortoiseLlmUsageEnum){
        DefaultTortoiseMessageHandler<ChatCompletionResponse> defaultTortoiseMessageHandler = new DefaultTortoiseMessageHandler<>(ChatCompletionResponse.class);
        AfterChatDTO build = AfterChatDTO.builder()
                .mainInfo(defaultTortoiseMessageHandler.convertForMainInfo(messages, null, reply, null))
                .conversationId(conversationId)
                .build();
        tortoiseLlmUsageService.recordUsage(build, tortoiseLlmUsageEnum);
    }


}