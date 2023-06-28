package com.umc.refit.domain.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetDto {
    private String name;
    private String loginId;
    private String email;
}
