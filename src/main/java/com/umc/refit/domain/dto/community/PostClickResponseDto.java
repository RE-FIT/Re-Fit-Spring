package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostClickResponseDto {
    private String clickedMember;
    private Long postId;
    private String title;
    private String author;
    private String profileUrl;
    private List<String> imgUrls;
    private Integer size;
    private Integer category;
    private Integer gender;
    private Integer deliveryType;
    private Integer deliveryFee;
    private String address;
    private Integer price;
    private String detail;
    private Integer postType;
    private Integer postState;

    private LocalDateTime createdAt;

    private boolean scrapFlag;

    public PostClickResponseDto(String clickedMember, Long postId, String title, String author, String profileUrl, List<String> imgUrls, Integer size, Integer category, Integer gender, Integer deliveryType, Integer deliveryFee, String address, Integer price, String detail, Integer postType, Integer postState, LocalDateTime createdAt, boolean scrapFlag) {
        this.clickedMember = clickedMember;
        this.postId = postId;
        this.title = title;
        this.author = author;
        this.profileUrl = profileUrl;
        this.imgUrls = imgUrls;
        this.size = size;
        this.category = category;
        this.gender = gender;
        this.deliveryType = deliveryType;
        this.deliveryFee = deliveryFee;
        this.address = address;
        this.price = price;
        this.detail = detail;
        this.postType = postType;
        this.postState = postState;
        this.createdAt = createdAt;
        this.scrapFlag = scrapFlag;

    }
}
