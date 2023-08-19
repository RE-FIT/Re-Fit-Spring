package com.umc.refit.domain.dto.mypage;

import com.umc.refit.domain.entity.Member;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMyInfoResponseDto {
    private String imageUrl;
    private String email;
    private String loginId;
    private String name;
    private String birth;
    private Integer gender;
    private String type;

    public static GetMyInfoResponseDto from(Member member) {
        return GetMyInfoResponseDto.builder()
                .imageUrl(member.getImageUrl())
                .email(member.getEmail())
                .loginId(member.getLoginId())
                .name(member.getName())
                .birth(member.getBirth())
                .gender(member.getGender())
                .type(member.getSocialType())
                .build();
    }
}
