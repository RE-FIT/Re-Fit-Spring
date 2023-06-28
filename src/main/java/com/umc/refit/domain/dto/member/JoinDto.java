package com.umc.refit.domain.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDto {
    private String loginId;
    private String password;
    private String email;
    private String name;
    private String birth;
}
