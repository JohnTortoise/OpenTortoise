package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.generator.UniqueIdGenerator;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.enums.TortoiseLlmUsageEnum;
import io.github.johntortoise.mapper.TortoiseMessageMapper;
import io.github.johntortoise.model.TortoiseConversation;
import io.github.johntortoise.model.TortoiseMessage;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseLlmUsageService;
import io.github.johntortoise.service.TortoiseMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
@Slf4j
public class TortoiseMessageServiceImpl extends ServiceImpl<TortoiseMessageMapper, TortoiseMessage> implements TortoiseMessageService {


    @Resource
    @Lazy
    private TortoiseConversationService tortoiseConversationService;

    @Resource
    @Lazy
    private TortoiseLlmUsageService tortoiseLlmUsageService;

    @Override
    public Page<Message> page(String conversationId, Long current, Long size) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话ID不能为空");
        }
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        
        LambdaQueryWrapper<TortoiseMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TortoiseMessage::getConversationId, conversationId)
                .eq(TortoiseMessage::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByAsc(TortoiseMessage::getCreateTime);
        Page<TortoiseMessage> page = this.baseMapper.selectPage(new Page<>(current, size), queryWrapper);

        List<Message> messages = new ArrayList<>();

        page.getRecords().forEach(tortoiseMessage -> {
            if (tortoiseMessage.getInputContent() != null && tortoiseMessage.getInputRole() != null) {
                messages.add(new Message(tortoiseMessage.getInputContent(), tortoiseMessage.getInputRole()));
            }
            if (tortoiseMessage.getOutputContent() != null && tortoiseMessage.getOutputRole() != null) {
                messages.add(new Message(tortoiseMessage.getOutputContent(), tortoiseMessage.getOutputRole()));
            }
        });

        Page<Message> targetPage = new Page<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
        targetPage.setRecords(messages);
        targetPage.setPages(page.getPages());

        return targetPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void writeMessagesAndRecordUsage(String conversationId, Message input, Message outPut, AfterChatDTO afterChatDTO) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话ID不能为空");
        }
        if (input == null) {
            throw new IllegalArgumentException("输入消息不能为空");
        }
        if (outPut == null) {
            throw new IllegalArgumentException("输出消息不能为空");
        }
        
        try {
            LogUtil.debug("写入消息: conversationId={}", conversationId);
            
            TortoiseConversationDTO tortoiseConversation =
                    tortoiseConversationService.getByConversationId(conversationId);

            if (Objects.isNull(tortoiseConversation)) {
                throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "会话不存在");
            }

            TortoiseMessage build = TortoiseMessage.builder()
                    .conversationId(conversationId)
                    .inputContent(input.getContent())
                    .inputRole(input.getRole())
                    .outputContent(outPut.getContent())
                    .outputRole(outPut.getRole())
                    .uniqueId(UniqueIdGenerator.generateId())
                    .build();
            this.save(build);

            
            Integer qAndACount = tortoiseConversation.getQAndACount();
            if (Objects.isNull(qAndACount)) {
                qAndACount = 0;
            }
            TortoiseConversation conversation = new TortoiseConversation();
            conversation.setQAndACount(qAndACount + 1);
            conversation.setId(tortoiseConversation.getId());
            tortoiseConversationService.updateById(conversation);
            if(Objects.nonNull(afterChatDTO)){
                tortoiseLlmUsageService.recordUsage(afterChatDTO, TortoiseLlmUsageEnum.CONVERSATION);
            }
            LogUtil.debug("写入消息成功: conversationId={}, qAndACount={}", conversationId, qAndACount + 1);
        } catch (TortoiseBusinessException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("写入消息失败: conversationId={}", conversationId, e);
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR, "写入消息失败", e);
        }
    }

    @Override
    public List<TortoiseMessage> listRecentByConversationId(String conversationId, Long limit) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话ID不能为空");
        }
        if (limit == null || limit < 1) {
            throw new IllegalArgumentException("限制数量必须大于0");
        }
        
        return this.list(new LambdaQueryWrapper<TortoiseMessage>()
                .eq(TortoiseMessage::getConversationId, conversationId)
                .eq(TortoiseMessage::getIsDeleted, DeletedEnum.EXIST.getCode())
                .orderByDesc(TortoiseMessage::getCreateTime)
                .last("limit " + limit));
    }

    @Override
    public List<TortoiseMessage> listByConversationIdAndGreaterThanId(String conversationId, Long lastMessageId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("对话ID不能为空");
        }
        if (lastMessageId == null) {
            throw new IllegalArgumentException("最后消息ID不能为空");
        }
        
        return this.list(new LambdaQueryWrapper<TortoiseMessage>()
                .eq(TortoiseMessage::getConversationId, conversationId)
                .eq(TortoiseMessage::getIsDeleted, DeletedEnum.EXIST.getCode())
                .gt(TortoiseMessage::getId, lastMessageId)
                .orderByDesc(TortoiseMessage::getCreateTime));
    }

    @Override
    public TortoiseMessage findGtLastMessageId(String conversationId, Long lastMessage, Boolean asc) {
        LambdaQueryWrapper<TortoiseMessage> queryWrapper = new QueryWrapper<TortoiseMessage>().lambda()
                .eq(TortoiseMessage::getConversationId, conversationId);
        if(Objects.nonNull(lastMessage)){
            queryWrapper.gt(TortoiseMessage::getId,lastMessage);
        }
        if(!asc){
            queryWrapper.orderByDesc(TortoiseMessage::getId).last("limit 1");
        }
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<TortoiseMessage> findByUniqueIdList(Set<String> uniqueIdSet) {
        return this.list(new QueryWrapper<TortoiseMessage>().lambda()
                .in(TortoiseMessage::getUniqueId,uniqueIdSet));
    }
}
