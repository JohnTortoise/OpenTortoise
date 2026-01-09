package io.github.johntortoise.dto;

import io.github.johntortoise.core.dto.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO extends Message {
    private Long id;
}
