package io.github.johntortoise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TortoiseLlmConfigDTO {

    private Long id;


    private String configName;


    private String modelName;


    private String apiUrl;


    @NotNull(message = "输入单元价格不能为空")
    private BigDecimal inputUnitPrice;


    @NotNull(message = "输出单元价格不能为空")
    private BigDecimal outputUnitPrice;


    @Builder.Default
    private BigDecimal cachePrice = BigDecimal.ZERO;


    @Builder.Default
    private Integer status = 1;


    @NotBlank(message = "配置描述不能为空")
    private String description;


    @Builder.Default
    private BigDecimal temperature = new BigDecimal("1.0");


    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
