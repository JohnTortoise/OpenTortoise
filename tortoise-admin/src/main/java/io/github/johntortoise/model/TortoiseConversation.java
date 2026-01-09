package io.github.johntortoise.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.johntortoise.dto.TortoiseConversationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TortoiseConversation {
    

    @TableId(type = IdType.AUTO)
    private Long id;


    private String conversationId;


    private Integer qAndACount;


    private Long chatProfileId;

    

    private Long createUserId;

    

    private Integer isDeleted;
    

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public static TortoiseConversationDTO covertToDTO(TortoiseConversation tortoiseConversation,
                                                      Map<Long, TortoiseChatProfile> chatProfileMap,
                                                      Map<Long,String> llmConfigIdToNameMap,
                                                      Map<Long,String> memoryConfigIdToNameMap){

        TortoiseChatProfile tortoiseChatProfile = chatProfileMap.get(tortoiseConversation.getChatProfileId());

        return TortoiseConversationDTO.builder()
                .id(tortoiseConversation.getId())
                .tortoiseChatProfileId(tortoiseConversation.getChatProfileId())
                .tortoiseChatProfileIdName(tortoiseChatProfile.getName())
                .conversationId(tortoiseConversation.getConversationId())
                .memoryPolicyId(tortoiseChatProfile.getMemoryPolicyId())
                .memoryPolicyName(memoryConfigIdToNameMap.get(tortoiseChatProfile.getMemoryPolicyId()))
                .llmConfigId(tortoiseChatProfile.getLlmConfigId())
                .llmConfigName(llmConfigIdToNameMap.get(tortoiseChatProfile.getLlmConfigId()))
                .createUserId(tortoiseConversation.getCreateUserId())
                .createTime(tortoiseConversation.getCreateTime())
                .updateTime(tortoiseConversation.getUpdateTime())
                .qAndACount(tortoiseConversation.getQAndACount())
                .build();
    }
}