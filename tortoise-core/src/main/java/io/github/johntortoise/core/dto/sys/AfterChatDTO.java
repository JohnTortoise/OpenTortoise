package io.github.johntortoise.core.dto.sys;

import io.github.johntortoise.core.dto.model.MainInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfterChatDTO {
    private String conversationId;
    private MainInfo mainInfo;
}
