package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.s3.ImageDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostImage {
    @Id
    @GeneratedValue
    @Column(name = "postImage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    private String s3key;
    private String imageUrl;


    public PostImage(ImageDto imageDto) {
        this.s3key = imageDto.getS3key();
        this.imageUrl = imageDto.getImageUrl();
    }


    // Posts 정보 저장
    public void setPost(Posts post){
        this.post = post;

        // 게시글에 현재 파일이 존재하지 않는다면
        if(!post.getImage().contains(this)){
            // 파일 추가
            post.getImage().add(this);
        }
    }
}