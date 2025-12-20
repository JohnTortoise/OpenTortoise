package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.mapper.TortoiseTokenTotalRecordMapper;
import io.github.johntortoise.model.TortoiseChatProfile;
import io.github.johntortoise.model.TortoiseTokenTotalRecord;
import io.github.johntortoise.service.TortoiseChatProfileService;
import io.github.johntortoise.service.TortoiseConversationService;
import io.github.johntortoise.service.TortoiseTokenTotalRecordService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class TortoiseTokenTotalRecordServiceServiceImpl extends ServiceImpl<TortoiseTokenTotalRecordMapper,
        TortoiseTokenTotalRecord> implements TortoiseTokenTotalRecordService {

    @Resource
    @Lazy
    private TortoiseChatProfileService tortoiseChatProfileService;

    @Resource
    @Lazy
    private TortoiseConversationService tortoiseConversationService;


    @Override
    public TortoiseTokenTotalRecord queryIfNoExistAutoCreateByConversationId(String conversationId) {

        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(99);

        TortoiseTokenTotalRecord chatProfileRecord =
                this.getOne(new QueryWrapper<TortoiseTokenTotalRecord>()
                        .lambda().eq(TortoiseTokenTotalRecord::getConversationId, conversationId)
                        .ge(TortoiseTokenTotalRecord::getCreateTime, startTime)
                        .le(TortoiseTokenTotalRecord::getCreateTime, endTime));

        if(Objects.nonNull(chatProfileRecord)){
            return chatProfileRecord;
        }
        TortoiseTokenTotalRecord tortoiseTokenTotalRecord = TortoiseTokenTotalRecord.builder()
                .conversationId(conversationId)
                .createTime(startTime).total(0L)
                .build();
        this.save(tortoiseTokenTotalRecord);
        return tortoiseTokenTotalRecord;

    }

    @Override
    public TortoiseTokenTotalRecord queryIfNoExistAutoCreateByChatProfileId(Long chatProfileId) {

        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(99);

        TortoiseTokenTotalRecord chatProfileRecord =
                this.getOne(new QueryWrapper<TortoiseTokenTotalRecord>()
                        .lambda().eq(TortoiseTokenTotalRecord::getChatProfileId, chatProfileId)
                        .ge(TortoiseTokenTotalRecord::getCreateTime, startTime)
                        .le(TortoiseTokenTotalRecord::getCreateTime, endTime));

        if(Objects.nonNull(chatProfileRecord)){
            return chatProfileRecord;
        }
        TortoiseTokenTotalRecord tortoiseTokenTotalRecord = TortoiseTokenTotalRecord.builder()
                .chatProfileId(chatProfileId)
                .createTime(startTime).total(0L)
                .build();
        this.save(tortoiseTokenTotalRecord);
        return tortoiseTokenTotalRecord;
    }

    @Override
    public void writeConsume(MainInfo.Tokens tokens, TortoiseConversationDTO conversation) {
        if (Objects.nonNull(tokens) && tokens.getTotalTokens() != null) {
            Integer totalTokens = tokens.getTotalTokens();
            Long tortoiseChatProfileId = conversation.getTortoiseChatProfileId();
            String conversationId = conversation.getConversationId();

            TortoiseChatProfile tortoiseChatProfile = tortoiseChatProfileService.getById(
                    tortoiseChatProfileId);
            if (tortoiseChatProfile == null) {
                LogUtil.error("聊天配置不存在: chatProfileId:{}",tortoiseChatProfileId);
            } else {
                TortoiseTokenTotalRecord charProfileRecord =
                        this.queryIfNoExistAutoCreateByChatProfileId(tortoiseChatProfile.getId());
                charProfileRecord.setTotal(charProfileRecord.getTotal() + totalTokens);
                this.updateById(charProfileRecord);

                TortoiseTokenTotalRecord conversationRecord =
                        this.queryIfNoExistAutoCreateByConversationId(conversationId);
                conversationRecord.setTotal(conversationRecord.getTotal() + totalTokens);
                this.updateById(conversationRecord);
            }
        }
    }
}