package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TortoiseToolDTO {

    private Long id;

    private String toolName;

    private String desc;
}
