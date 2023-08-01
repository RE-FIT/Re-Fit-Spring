package com.umc.refit.domain.dto.mypage;

import lombok.*;

import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMyInfoRequestDto {
    @Size(min = 1, max = 255)
    private String name;
    private String birth;
    private Integer gender;
}
