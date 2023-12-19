package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportMemDto {
    private Member reporter;

    private Member reportedMember;

    private String reason;

}
