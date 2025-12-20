package io.github.johntortoise.core.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Model {
    private String completeUrl;

    private String apiKey;

    private String modelName;

    private BigDecimal temperature = new BigDecimal("0.7");

}
