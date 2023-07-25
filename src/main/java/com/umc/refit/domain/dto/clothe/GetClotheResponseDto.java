package com.umc.refit.domain.dto.clothe;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetClotheResponseDto {
    private Long id;
    private Integer category;
    private Integer season;
    private Integer targetCnt;
    private Integer targetPeriod;
    private boolean isPlan;
    private Integer cntPerMonth;
    private Integer cntPerWeek;
    private String imageUrl;
}
