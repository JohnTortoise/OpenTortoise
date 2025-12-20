package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemoryBase {


    private Integer type;


    private Long threshold;


    private Long batchAnalysisSize;


    private Boolean withHistory;


    private Long maxLength;


    private String basePrompt;


    private String overLengthPrompt;


    private String updatePrompt;
}
