package com.umc.refit.domain.dto.community;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostClickResponseDto {
    private Long postId;
    private String title;
    private String author;
    private List<String> imgUrls;
    private Integer size;
    private Integer deliveryType;
    private Integer deliveryFee;
    private String region;
    private Integer price;

    private String detail;
    private Integer postType;

    public PostClickResponseDto(Long postId, String title, String author, List<String> imgUrls, Integer size, Integer deliveryType, Integer deliveryFee, String region, Integer price, String detail, Integer postType) {
        this.postId = postId;
        this.title = title;
        this.author = author;
        this.imgUrls = imgUrls;
        this.size = size;
        this.deliveryType = deliveryType;
        this.deliveryFee = deliveryFee;
        this.region = region;
        this.price = price;
        this.detail = detail;
        this.postType = postType;
    }
}
