package com.nanda.platform.auth.domain.dto;

import lombok.Data;

@Data
public class RefreshTokenResponse {

    private String accessToken;
    private long expiresIn;
}
