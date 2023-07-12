package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockDto {
    private Member requestMember;

    private Member blockedMember;

}
