package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostDto {
    private Member member;
    private String title;
    private Integer gender;
    private Integer postType;
    private Integer category;
    private Integer size;
    private Integer deliveryType;
    private Integer deliveryFee;
    private Integer price;
    private String sido;
    private String sigungu;
    private String bname;
    private String bname2;
    private String detail;
    private Integer postState;
}
