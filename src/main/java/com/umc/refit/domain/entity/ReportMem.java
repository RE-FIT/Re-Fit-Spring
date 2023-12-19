package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.community.ReportMemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportMem {

    @Id
    @GeneratedValue
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id")
    private Member reportedMember;

    private String reason;


    public ReportMem(ReportMemDto requestDto) {
        this.reporter = requestDto.getReporter();
        this.reportedMember = requestDto.getReportedMember();
        this.reason = requestDto.getReason();
    }
}
