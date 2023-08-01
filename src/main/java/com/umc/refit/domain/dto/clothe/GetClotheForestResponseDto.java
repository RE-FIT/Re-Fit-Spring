package com.umc.refit.domain.dto.clothe;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetClotheForestResponseDto {
    private Long id;
    private String imageUrl;
    private Integer targetCnt;
    private int count;
    private int remainedCnt;
}
