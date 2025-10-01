package dev.waf.console.auth.api.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String code;
    private String state;
}