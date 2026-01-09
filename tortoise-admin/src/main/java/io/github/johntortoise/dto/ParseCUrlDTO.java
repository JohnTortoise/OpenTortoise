package io.github.johntortoise.dto;

import io.github.johntortoise.core.dto.sys.FieldDTO;
import io.github.johntortoise.core.dto.sys.HeadersDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParseCUrlDTO {

    private List<FieldDTO> fieldConfig;

    private String url;

    private String method;

    private List<HeadersDTO> headers;
}
