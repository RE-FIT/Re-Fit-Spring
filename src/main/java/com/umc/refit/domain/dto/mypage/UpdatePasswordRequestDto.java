package com.umc.refit.domain.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UpdatePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
}
