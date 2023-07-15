package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class PostDto {
    private Member member;

    @NotNull(message = "제목이 없습니다.")
    private String title;

    @NotNull(message = "추천 성별이 없습니다.")
    @Min(0)
    @Max(1)
    private Integer gender;

    @NotNull(message = "나눔,판매는 필수 입력값입니다.")
    @Min(0)
    @Max(1)
    private Integer postType;

    @NotNull(message = "카테고리는 필수 입력값입니다.")
    @Min(0)
    @Max(5)
    private Integer category;

    @NotNull(message = "사이즈는 필수 입력값입니다.")
    @Min(0)
    @Max(4)
    private Integer size;

    @NotNull(message = "배송 방법은 필수 입력값입니다.")
    private Integer deliveryType;
    private Integer deliveryFee;
    private Integer price;
    private String region;

    @NotNull(message = "상세 설명은 필수 입력값입니다.")
    private String detail;
    private Integer postState;
}
