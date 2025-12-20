package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.enums.RoleEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.dto.MemoryBase;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.MemoryTypeEnum;
import io.github.johntortoise.mapper.TortoiseMemoryMapper;
import io.github.johntortoise.model.TortoiseMemory;
import io.github.johntortoise.model.TortoiseMemoryPolicy;
import io.github.johntortoise.model.TortoiseMessage;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseMemoryPolicyService;
import io.github.johntortoise.service.TortoiseMemoryService;
import io.github.johntortoise.service.TortoiseMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class TortoiseMemoryServiceImpl extends ServiceImpl<TortoiseMemoryMapper, TortoiseMemory> implements TortoiseMemoryService {

    private static final ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

    @Resource
    private TortoiseMessageService tortoiseMessageService;

    @Resource
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    private TortoiseMemoryPolicyService tortoiseMemoryPolicyService;


    
    @Override
    public List<Message> getMemoriesByConversationId(String conversationId) {
        try {
            LogUtil.info("getMemoriesByConversationId:{}",conversationId);

            TortoiseConversationDTO tortoiseConversationDTO = tortoiseConversationService.getByConversationId(conversationId);
            Long memoryPolicyId = tortoiseConversationDTO.getMemoryPolicyId();
            TortoiseMemoryPolicy tortoiseMemoryPolicy = tortoiseMemoryPolicyService.getById(memoryPolicyId);
            MemoryBase memoryBase = ObjectMapperUtil.createObjectMapper().readValue(tortoiseMemoryPolicy.getConfig(), MemoryBase.class);
            if(memoryBase.getType().equals(MemoryTypeEnum.RECENT.getCode())){
                return findMessageByThreshold(conversationId,memoryBase.getThreshold());
            }else {
                TortoiseMemory tortoiseMemory = this.getOne(new QueryWrapper<TortoiseMemory>().lambda()
                        .eq(TortoiseMemory::getConversationId, conversationId)
                        .eq(TortoiseMemory::getIsDeleted, DeletedEnum.EXIST.getCode())
                        .orderByDesc(TortoiseMemory::getCreateTime)
                        .last("limit 1"));
                
                if(Objects.isNull(tortoiseMemory)){
                    return findMessageByThreshold(conversationId,memoryBase.getThreshold());
                }else {
                    List<Message> defaultMessage = new ArrayList<>();
                    String prefixPrompt = Objects.requireNonNull(MemoryTypeEnum.getByCode(memoryBase.getType())).getDesc();
                    defaultMessage.add(new Message("【对话历史"+prefixPrompt+"】以下是之前对话的"+prefixPrompt+"：{" + tortoiseMemory.getContent() + "}" + "\n", RoleEnum.ASSISTANT.getCode()));

                    Long lastMessageId = tortoiseMemory.getLastMessageId();
                    List<TortoiseMessage> messages = tortoiseMessageService.listByConversationIdAndGreaterThanId(conversationId, lastMessageId);
                    messages.forEach(message->{
                        defaultMessage.add(new Message(message.getInputContent(),message.getInputRole()));
                        defaultMessage.add(new Message(message.getOutputContent(),message.getOutputRole()));
                    });
                    return defaultMessage;

                }
            }
        }catch (Exception e) {
            LogUtil.error("getMemoriesByConversationId",e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, e);
        }
    }


    public List<Message> findMessageByThreshold(String conversationId,Long threshold){
        List<Message> defaultMessage = new ArrayList<>();
        List<TortoiseMessage> messages = tortoiseMessageService.listRecentByConversationId(conversationId, threshold);

        messages.sort(Comparator.comparing(TortoiseMessage::getCreateTime));

        messages.forEach(message->{
            defaultMessage.add(new Message(message.getInputContent(),message.getInputRole()));
            defaultMessage.add(new Message(message.getOutputContent(),message.getOutputRole()));
        });
        return defaultMessage;
    }

    @Override
    public TortoiseMemory findMemoryByConversation(String conversationId) {
        return this.getOne(new QueryWrapper<TortoiseMemory>().lambda()
                .eq(TortoiseMemory::getConversationId, conversationId)
                .eq(TortoiseMemory::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseMemory::getCreateTime)
                
                .last("limit 1 FOR UPDATE"));
    }

    public static List<Message> convertToMessageList(String content) {
        try {
            return objectMapper.readValue(content, new TypeReference<List<Message>>() {});
        } catch (Exception e) {
            LogUtil.error("convertToMessageList",e);
            throw new RuntimeException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }
}