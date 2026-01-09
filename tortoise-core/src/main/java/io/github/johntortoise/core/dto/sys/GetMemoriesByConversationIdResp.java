package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GetMemoriesByConversationIdResp {
    List<Message> messages;
}
