package io.github.johntortoise.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportConversationMessageDTO {


    @ExcelProperty(index = 0, value = "conversation_id")
    private String conversationId;


    @ExcelProperty(index = 1, value = "chat_profile_id")
    private Long chatProfileId;


    @ExcelProperty(index = 2, value = "input_content")
    private String inputContent;


    @ExcelProperty(index = 3, value = "input_role")
    private String inputRole;


    @ExcelProperty(index = 4, value = "output_content")
    private String outputContent;


    @ExcelProperty(index = 5, value = "output_role")
    private String outputRole;


    @ExcelProperty(index = 6, value = "unique_id")
    private String uniqueId;
}
