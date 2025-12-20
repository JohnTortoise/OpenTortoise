package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.model.MainInfo;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import io.github.johntortoise.model.TortoiseTokenTotalRecord;


public interface TortoiseTokenTotalRecordService extends IService<TortoiseTokenTotalRecord> {
    TortoiseTokenTotalRecord queryIfNoExistAutoCreateByConversationId(String conversationId);


    TortoiseTokenTotalRecord queryIfNoExistAutoCreateByChatProfileId(Long chatProfileId);

    void writeConsume(MainInfo.Tokens tokens, TortoiseConversationDTO conversation);

}