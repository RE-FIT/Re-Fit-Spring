package com.umc.refit.domain.dto.community;

import com.umc.refit.domain.entity.Member;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ReportMemDto {
    private Member reporter;

    private Member reportedMember;

    @NotNull(message = "신고 이유는 필수 입력값입니다.")
    @Min(0)
    @Max(5)
    private Integer reason;

    private String reasonDetail;
}
