package com.umc.refit.domain.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMainResponseDto {
    private Long postId;
    private String title;
    private String imgUrl;
    private Integer gender;
    private Integer deliveryType;
    private String sido;
    private String sigungu;
    private String bname;
    private String bname2;
    private Integer price;

    public PostMainResponseDto(Long postId, String title, String imgUrl, Integer gender, Integer deliveryType, String sido, String sigungu, String bname, String bname2, Integer price) {
        this.postId = postId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.gender = gender;
        this.deliveryType = deliveryType;
        this.sido = sido;
        this.sigungu = sigungu;
        this.bname = bname;
        this.bname2 = bname2;
        this.price = price;
    }
}
