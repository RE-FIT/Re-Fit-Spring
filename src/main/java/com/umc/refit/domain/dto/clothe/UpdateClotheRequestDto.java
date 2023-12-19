package com.umc.refit.domain.dto.clothe;

import lombok.*;

import jakarta.validation.constraints.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClotheRequestDto {
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

    private Boolean isPlan; // 현재 월이 season 에 포함되는 경우

    private Integer cntPerMonth;

    private Integer cntPerWeek;


}
