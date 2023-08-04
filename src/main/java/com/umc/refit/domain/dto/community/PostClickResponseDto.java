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
    private List<String> imgUrls;
    private Integer size;
    private Integer deliveryType;
    private Integer deliveryFee;
    private String sido;
    private String sigungu;
    private String bname;
    private String bname2;
    private Integer price;
    private String detail;
    private Integer postType;
    private Integer postState;

    private LocalDateTime createdAt;

    private boolean scrapFlag;

    public PostClickResponseDto(String clickedMember, Long postId, String title, String author, List<String> imgUrls, Integer size, Integer deliveryType, Integer deliveryFee, String sido, String sigungu, String bname, String bname2, Integer price, String detail, Integer postType, Integer postState, LocalDateTime createdAt, boolean scrapFlag) {
        this.clickedMember = clickedMember;
        this.postId = postId;
        this.title = title;
        this.author = author;
        this.imgUrls = imgUrls;
        this.size = size;
        this.deliveryType = deliveryType;
        this.deliveryFee = deliveryFee;
        this.sido = sido;
        this.sigungu = sigungu;
        this.bname = bname;
        this.bname2 = bname2;
        this.price = price;
        this.detail = detail;
        this.postType = postType;
        this.postState = postState;
        this.createdAt = createdAt;
        this.scrapFlag = scrapFlag;
    }
}
