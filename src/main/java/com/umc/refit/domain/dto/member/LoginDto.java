package com.umc.refit.domain.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginDto {
    private String loginId;
    private String password;
    private String fcm;
}