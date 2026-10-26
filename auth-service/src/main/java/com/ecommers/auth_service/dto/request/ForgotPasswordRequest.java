package com.ecommers.auth_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    private String email;
}
