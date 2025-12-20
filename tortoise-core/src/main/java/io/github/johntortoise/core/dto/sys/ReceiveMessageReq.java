package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiveMessageReq {
    private String conversationId;

    private Message input;

    private Message outPut;

}
