package com.umc.refit.domain.dto.clothe;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowClotheMainRequestDto {
    private int category;
    private int season;
    private String sort;
    private int page;
    private int size;
}
