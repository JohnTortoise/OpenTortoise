package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.dto.model.ChatCompletionResponse;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.model.Usage;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.dto.TortoiseLlmUsageDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.TortoiseLlmUsageEnum;
import io.github.johntortoise.mapper.TortoiseLlmUsageMapper;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseLlmConfig;
import io.github.johntortoise.model.TortoiseLlmUsage;
import io.github.johntortoise.model.TortoiseMemoryPolicy;
import io.github.johntortoise.service.*;
import io.github.johntortoise.utils.PageConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TortoiseLlmUsageServiceImpl extends ServiceImpl<TortoiseLlmUsageMapper, TortoiseLlmUsage> implements TortoiseLlmUsageService {


    @Resource
    @Lazy
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    private TortoiseLlmConfigService llmConfigService;

    @Resource
    @Lazy
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;

    @Resource
    @Lazy
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Resource
    private TortoiseTokenTotalRecordService tortoiseTokenTotalRecordService;

    @Override
    public List<TortoiseLlmUsage> getUsageByConversationId(String conversationId) {
        return baseMapper.selectByConversationId(conversationId);
    }

    @Override
    public Page<TortoiseLlmUsageDTO> page(String conversationId, Long current, Long size) {

        LambdaQueryWrapper<TortoiseLlmUsage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseLlmUsage::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseLlmUsage::getCreateTime)
                .orderByDesc(TortoiseLlmUsage::getId);

        if (conversationId != null && !conversationId.isEmpty()) {
            queryWrapper.eq(TortoiseLlmUsage::getConversationId, conversationId);
        }


        Page<TortoiseLlmUsage> page = this.page(new Page<>(current, size), queryWrapper);

        Map<Long, String> modelIdToModelNameMap = new HashMap<>();
        if(!page.getRecords().isEmpty()){
            Set<Long> configId =
                    page.getRecords().stream().map(TortoiseLlmUsage::getConfigId).collect(Collectors.toSet());

            List<TortoiseLlmConfig> configs = llmConfigService.listByIds(configId);
            modelIdToModelNameMap = configs.stream().collect(Collectors.toMap(TortoiseLlmConfig::getId,
                    TortoiseLlmConfig::getModelName));
        }

        Map<Long, String> finalModelIdToModelNameMap = modelIdToModelNameMap;
        return PageConvertUtil.convert(page,
                usage -> TortoiseLlmUsage.covertToDTO(usage, finalModelIdToModelNameMap));
    }



    @Override
    public TortoiseLlmUsage generateDetail(String req, String reply, TortoiseLlmUsageEnum tortoiseLlmUsageEnum, String conversationId) {
        try {
            TortoiseConversationDTO conversation = tortoiseConversationService.getByConversationId(conversationId);
            if(Objects.isNull(conversation)){
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"会话不存在");
            }
            TortoiseLlmConfig llmConfig = findLlmConfig(tortoiseLlmUsageEnum, conversation);

            ChatCompletionResponse chatCompletionResponse = ObjectMapperUtil.createObjectMapper().readValue(reply, ChatCompletionResponse.class);
            return generateDetailCore(req, chatCompletionResponse, conversationId, llmConfig, tortoiseLlmUsageEnum);

        }catch (Exception e){
            LogUtil.error("generateDetail",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }


    @Override
    public TortoiseLlmUsage queryFirstUsageByLLmConfigId(Long llmConfigId) {
        return  this.getOne(new QueryWrapper<TortoiseLlmUsage>().lambda()
                .eq(TortoiseLlmUsage::getConfigId, llmConfigId)
                .last("limit 1"));
    }

    public List<Message> buildRequestMessage(AfterChatDTO afterChatDTO){
        MainInfo mainInfo = afterChatDTO.getMainInfo();
        Message input = mainInfo.getInput();
        List<Message> history = mainInfo.getHistory();
        if(Objects.nonNull(input)){
            history.add(input);
        }
        return history;
    }



    public TortoiseLlmUsage generateDetailCore(String req,ChatCompletionResponse chatCompletionResponse,String conversationId,TortoiseLlmConfig llmConfig,TortoiseLlmUsageEnum tortoiseLlmUsageEnum){
        try {
            Usage usage = chatCompletionResponse.getUsage();

            Integer inputToken = usage.getPromptTokens();
            Integer outPutToken = usage.getCompletionTokens();
            Integer cacheToken = Optional.ofNullable(usage.getPromptTokensDetails()).map(Usage.PromptTokensDetails::getCachedTokens).orElse(0);
            Integer totalToken = usage.getTotalTokens();

            BigDecimal inputCost = llmConfig.getInputUnitPrice()
                    .multiply(BigDecimal.valueOf(inputToken));
            BigDecimal outputCost = llmConfig.getOutputUnitPrice()
                    .multiply(BigDecimal.valueOf(outPutToken));
            BigDecimal cacheCost = llmConfig.getCachePrice()
                    .multiply(BigDecimal.valueOf(cacheToken));

            BigDecimal totalCost = inputCost.add(outputCost).add(cacheCost);

            ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

            return TortoiseLlmUsage.builder()
                    .conversationId(conversationId)
                    .configId(llmConfig.getId())
                    .inputTokens(inputToken)
                    .outputTokens(outPutToken)
                    .totalTokens(totalToken)
                    .inputCost(inputCost)
                    .type(tortoiseLlmUsageEnum.getId())
                    .outputCost(outputCost)
                    .totalCost(totalCost)
                    .requestContent(req)
                    .responseContent(objectMapper.writeValueAsString(chatCompletionResponse))
                    .isDeleted(DeletedEnum.EXIST.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
        }catch (Exception e){
            LogUtil.error("generateDetailCore",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }


    public void saveDetail(AfterChatDTO afterChatDTO, TortoiseLlmConfig llmConfig, TortoiseLlmUsageEnum tortoiseLlmUsageEnum, List<Message> history, Message outPut){
        try {
            MainInfo mainInfo = afterChatDTO.getMainInfo();
            MainInfo.Tokens tokens = mainInfo.getTokens();
            BigDecimal inputCost = llmConfig.getInputUnitPrice()
                    .multiply(BigDecimal.valueOf(tokens.getPromptTokens()));
            BigDecimal outputCost = llmConfig.getOutputUnitPrice()
                    .multiply(BigDecimal.valueOf(tokens.getCompletionTokens()));
            BigDecimal cacheCost = llmConfig.getCachePrice()
                    .multiply(BigDecimal.valueOf(tokens.getCachedTokens()));

            BigDecimal totalCost = inputCost.add(outputCost).add(cacheCost);

            ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

            TortoiseLlmUsage tortoiseLlmUsage = TortoiseLlmUsage.builder()
                    .conversationId(afterChatDTO.getConversationId())
                    .configId(llmConfig.getId())
                    .inputTokens(tokens.getPromptTokens())
                    .outputTokens(tokens.getCompletionTokens())
                    .totalTokens(tokens.getTotalTokens())
                    .inputCost(inputCost)
                    .type(tortoiseLlmUsageEnum.getId())
                    .outputCost(outputCost)
                    .totalCost(totalCost)
                    .requestContent(objectMapper.writeValueAsString(history))
                    .responseContent(objectMapper.writeValueAsString(outPut))
                    .isDeleted(DeletedEnum.EXIST.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            this.save(tortoiseLlmUsage);
        }catch (Exception e){
            LogUtil.error("recordUsage",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,e);
        }
    }

    public void saveTotal(MainInfo.Tokens tokens, TortoiseConversationDTO conversation){
        tortoiseTokenTotalRecordService.writeConsume(tokens, conversation);
    }


    public TortoiseLlmConfig findLlmConfig(TortoiseLlmUsageEnum tortoiseLlmUsageEnum, TortoiseConversationDTO conversation){
        Long llmConfigId;
        if(tortoiseLlmUsageEnum.equals(TortoiseLlmUsageEnum.CONVERSATION)){
            llmConfigId = conversation.getLlmConfigId();
        }else {
            TortoiseChatProfile chatProfile = tortoiseChatProfileService.getById(conversation.getTortoiseChatProfileId());
            TortoiseMemoryPolicy tortoiseMemoryPolicy = tortoiseMemoryPolicyService.getById(chatProfile.getMemoryPolicyId());
            llmConfigId = tortoiseMemoryPolicy.getLlmConfigId();
        }
        TortoiseLlmConfig llmConfig = llmConfigService.getById(llmConfigId);
        if(Objects.isNull(llmConfig)){
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"大模型配置不存在");
        }
        return llmConfig;
    }
}