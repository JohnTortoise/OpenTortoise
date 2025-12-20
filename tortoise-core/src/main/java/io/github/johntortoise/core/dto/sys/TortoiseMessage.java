package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.Message;
import io.github.johntortoise.core.enums.RoleEnum;
import io.github.johntortoise.generator.UniqueIdGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TortoiseMessage {

    public TortoiseMessage(String conversationId, String msg, RoleEnum roleEnum){
        this.conversationId = conversationId;
        this.message = new Message(msg,roleEnum.getCode());
    }

    public TortoiseMessage(String msg){
        this.conversationId = UniqueIdGenerator.generateId();
        this.message = new Message(msg, RoleEnum.USER.getCode());
    }

    public TortoiseMessage(String msg, RoleEnum roleEnum){
        this.conversationId = UniqueIdGenerator.generateId();
        this.message = new Message(msg,roleEnum.getCode());
    }

    private String conversationId;

    private Message message;

}
