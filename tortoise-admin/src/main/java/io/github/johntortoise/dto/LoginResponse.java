package io.github.johntortoise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String roleCode;
        private String roleName;
    }
}