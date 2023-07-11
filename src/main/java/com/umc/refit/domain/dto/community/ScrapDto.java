package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.Posts;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScrapDto {
    private Member member;
    private Posts post;

    public ScrapDto(Member member, Posts post) {
        this.member = member;
        this.post = post;
    }
}
