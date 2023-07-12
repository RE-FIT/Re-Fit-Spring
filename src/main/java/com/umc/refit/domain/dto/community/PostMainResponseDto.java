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
    private String region;
    private Integer price;

    public PostMainResponseDto(Long postId, String title, String imgUrl, Integer gender, Integer deliveryType, String region, Integer price) {
        this.postId = postId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.gender = gender;
        this.deliveryType = deliveryType;
        this.region = region;
        this.price = price;
    }
}
