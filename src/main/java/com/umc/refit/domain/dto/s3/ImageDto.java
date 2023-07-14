package com.umc.refit.domain.dto.s3;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ImageDto {
    private String s3key;
    private String imageUrl;

    public ImageDto(String s3key, String imageUrl) {
        this.s3key = s3key;
        this.imageUrl = imageUrl;
    }
}