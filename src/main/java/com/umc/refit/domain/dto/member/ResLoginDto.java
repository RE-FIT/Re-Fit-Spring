package com.umc.refit.domain.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ResLoginDto {

    private String refreshToken;
}
