package dev.waf.console.api.auth.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String code;
    private String state;
}