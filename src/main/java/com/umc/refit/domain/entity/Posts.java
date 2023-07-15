package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.community.PostDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Posts {
    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String title;
    private Integer gender;
    private Integer category;
    private Integer size;
    private Integer postType;
    private Integer deliveryType;
    private Integer deliveryFee;
    private Integer price;
    private String region;
    private String detail;
    private Integer postState;

    @OneToMany(
            mappedBy = "post",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<PostImage> image = new ArrayList<>();

    public Posts(PostDto postDto) {
        this.member = postDto.getMember();
        this.title = postDto.getTitle();
        this.gender = postDto.getGender();
        this.postType = postDto.getPostType();
        this.category = postDto.getCategory();
        this.size = postDto.getSize();
        this.price = postDto.getPrice();
        this.region = postDto.getRegion();
        this.deliveryType = postDto.getDeliveryType();
        this.deliveryFee = postDto.getDeliveryFee();
        this.detail = postDto.getDetail();
        this.postState = postDto.getPostState();
    }

    public void update(PostDto postDto){
        this.title = postDto.getTitle();
        this.gender = postDto.getGender();
        this.postType = postDto.getPostType();
        this.category = postDto.getCategory();
        this.size = postDto.getSize();
        this.price = postDto.getPrice();
        this.region = postDto.getRegion();
        this.deliveryType = postDto.getDeliveryType();
        this.deliveryFee = postDto.getDeliveryFee();
        this.detail = postDto.getDetail();
        this.postState = postDto.getPostState();
    }

    public void changeState(Integer newState){
        this.postState = newState;
    }
}
