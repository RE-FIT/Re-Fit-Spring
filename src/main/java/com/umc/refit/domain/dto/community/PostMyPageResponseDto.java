package com.umc.refit.domain.dto.community;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMyPageResponseDto {
    private Long postId;
    private String title;
    private String imgUrl;
    private Integer gender;
    private Integer deliveryType;
    private String address;
    private Integer price;
    private Integer size;

    public PostMyPageResponseDto(Long postId, String title, String imgUrl, Integer gender, Integer deliveryType, String address, Integer price, Integer size) {
        this.postId = postId;
        this.title = title;
        this.imgUrl = imgUrl;
        this.gender = gender;
        this.deliveryType = deliveryType;
        this.address = address;
        this.price = price;
        this.size = size;
    }
}
