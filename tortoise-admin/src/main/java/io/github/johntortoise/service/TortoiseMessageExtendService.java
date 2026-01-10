package io.github.johntortoise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.johntortoise.core.dto.sys.AfterChatDTO;
import io.github.johntortoise.core.dto.sys.LLmInvokeResp;
import io.github.johntortoise.model.TortoiseMessageExtend;

import java.util.List;


public interface TortoiseMessageExtendService extends IService<TortoiseMessageExtend> {


    List<LLmInvokeResp.Event> getByMessageId(Long messageId);

    List<TortoiseMessageExtend> getByMessageIdList(List<Long> messageIdList);

    void saveExtend(AfterChatDTO afterChatDTO,Long id);
}


