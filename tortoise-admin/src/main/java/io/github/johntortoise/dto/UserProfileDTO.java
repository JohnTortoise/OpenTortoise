package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    

    private Long id;
    

    private String email;
    

    private Long roleCode;
    

    private String roleName;
    

    private LocalDateTime createTime;
    

    private LocalDateTime updateTime;
}



