package com.umc.refit.domain.dto.mypage;

import com.umc.refit.domain.entity.Member;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetMyResponseDto {
    private String imageUrl;
    private String name;
    private Integer day;

    public GetMyResponseDto(Member member) {
        this.imageUrl = member.getImageUrl();
        this.name = member.getName();
        this.day = Math.toIntExact(ChronoUnit.DAYS.between(member.getJoinDate(), LocalDate.now()))+1;
    }
}
