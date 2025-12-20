package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportExcelResponseDTO {


    private Integer processedRows;


    private Integer createdConversations;


    private Integer createdMessages;


    private Integer skippedRows;


    private String errorMessage;
}
