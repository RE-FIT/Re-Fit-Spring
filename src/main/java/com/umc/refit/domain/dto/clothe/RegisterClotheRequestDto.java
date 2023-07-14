package com.umc.refit.domain.dto.clothe;

import com.umc.refit.domain.entity.Clothe;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterClotheRequestDto {

    @NotNull(message = "범위에 해당되지 않는 요청값입니다.")
    @Min(0)
    @Max(4)
    private Integer category;

    @NotNull(message = "범위에 해당되지 않는 요청값입니다.")
    @Min(0)
    @Max(2)
    private Integer season;

    private Integer targetCnt;

    private Integer targetPeriod;

    private Boolean isPlan;

    private Integer cntPerMonth;

    private Integer cntPerWeek;

    public Clothe toEntity() {
        return Clothe.builder()
                .category(category)
                .season(season)
                .targetCnt(targetCnt)
                .targetPeriod(targetPeriod)
                .isPlan(isPlan)
                .count(0)
                .editCnt(0)
                .cntPerMonth(cntPerMonth)
                .cntPerMonth(cntPerMonth)
                .lastDate(null)
                .build();
    }
}
