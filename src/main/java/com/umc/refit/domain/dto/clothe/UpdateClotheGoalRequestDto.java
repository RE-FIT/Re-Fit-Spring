package com.umc.refit.domain.dto.clothe;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClotheGoalRequestDto {

    @NotNull
    @Min(0)
    @Max(2)
    private Integer season;

    private Integer targetCnt;

    private Integer targetPeriod;

    private Boolean isPlan;

    private Integer cntPerMonth;

    private Integer cntPerWeek;
}
