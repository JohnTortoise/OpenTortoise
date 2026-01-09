package io.github.johntortoise.core.dto.sys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FieldDTO {

    private String name;

    private String type;

    private String description;

    private List<String> enumValues;

    private Boolean required;

}
