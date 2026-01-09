package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SaveConversationToolsReq {

    @NotBlank(message = "会话ID不能为空")
    private String conversationId;

    @NotEmpty(message = "工具ID列表不能为空")
    private List<String> toolIds;
}









