package com.nanda.platform.auth.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;
    private List<String> permissions;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String displayName;
        private Long orgId;
    }
}
