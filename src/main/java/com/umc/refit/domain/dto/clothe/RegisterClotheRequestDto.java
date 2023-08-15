package com.umc.refit.domain.dto.clothe;

import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.domain.entity.Member;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterClotheRequestDto {

    @NotNull(message = "범위에 해당되지 않는 요청값입니다.")
    @Range(min = 0, max = 4)
    private Integer category;

    @NotNull(message = "범위에 해당되지 않는 요청값입니다.")
    @Range(min = 0, max = 2)
    private Integer season;

    private Integer targetCnt;

    private Integer targetPeriod;

    private Boolean isPlan; // 현재 월이 season 에 포함되는 경우

    private Integer cntPerMonth;

    private Integer cntPerWeek;

    public Clothe toEntity(Member member, ImageDto imageDto) {
        return Clothe.builder()
                .category(category)
                .season(season)
                .targetCnt(targetCnt)
                .targetPeriod(targetPeriod)
                .isPlan(isPlan)
                .count(0)
                .editCnt(0)
                .cntPerMonth(cntPerMonth)
                .cntPerWeek(cntPerWeek)
                .member(member)
                .imageUrl(imageDto.getImageUrl())
                .lastDate(null)
                .build();
    }
}
