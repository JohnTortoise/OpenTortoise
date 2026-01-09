package io.github.johntortoise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.core.enums.ErrorCodeEnum;
import io.github.johntortoise.core.exceptions.TortoiseBusinessException;
import io.github.johntortoise.core.utils.EmptyUtil;
import io.github.johntortoise.core.utils.LogUtil;
import io.github.johntortoise.core.utils.ObjectMapperUtil;
import io.github.johntortoise.enums.DeletedEnum;
import io.github.johntortoise.mapper.TortoiseMessageExtendMapper;
import io.github.johntortoise.model.TortoiseMessageExtend;
import io.github.johntortoise.service.TortoiseMessageExtendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class TortoiseMessageExtendServiceImpl extends ServiceImpl<TortoiseMessageExtendMapper, TortoiseMessageExtend> implements TortoiseMessageExtendService {

    @Override
    public List<LLmInvokeResp.Event> getByMessageId(Long messageId) {
        try {
            if (messageId == null) {
                throw new IllegalArgumentException("消息ID不能为空");
            }

            TortoiseMessageExtend one = this.getOne(new LambdaQueryWrapper<TortoiseMessageExtend>()
                    .eq(TortoiseMessageExtend::getMessageId, messageId)
                    .eq(TortoiseMessageExtend::getIsDeleted, DeletedEnum.EXIST.getCode()));
            if(EmptyUtil.isNotEmpty(one)){
                String eventsStr = one.getEvents();
                if(EmptyUtil.isNotEmpty(eventsStr)){
                    return ObjectMapperUtil.createObjectMapper().readValue(eventsStr, new TypeReference<List<LLmInvokeResp.Event>>() {});
                }
            }
            return new ArrayList<>();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }



    @Override
    public void saveExtend(AfterChatDTO afterChatDTO, Long messageId) {
        try {
            List<Message> toolCallMessages = afterChatDTO.getToolCallMessages();
            List<LLmInvokeResp.Event> events = afterChatDTO.getEvents();
            TortoiseMessageExtend tortoiseMessageExtend = TortoiseMessageExtend.builder().messageId(messageId).build();
            if(EmptyUtil.isNotEmpty(toolCallMessages)){
                tortoiseMessageExtend.setToolCallMessages(ObjectMapperUtil.createObjectMapper().writeValueAsString(toolCallMessages));
            }
            if(EmptyUtil.isNotEmpty(events)){
                tortoiseMessageExtend.setEvents(ObjectMapperUtil.createObjectMapper().writeValueAsString(events));
            }
            this.save(tortoiseMessageExtend);
        }catch (Exception e){
            LogUtil.info("saveByAfterChat error"+e.getMessage());
            throw new TortoiseBusinessException(ErrorCodeEnum.BUSINESS_ERROR,"saveByAfterChat error"+e.getMessage());
        }
    }
}


