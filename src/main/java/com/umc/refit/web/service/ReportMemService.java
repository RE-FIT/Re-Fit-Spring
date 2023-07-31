package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.ReportMemDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.ReportMem;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.web.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.umc.refit.exception.ExceptionType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportMemService {

    private final ReportRepository reportMemRepository;
    private final MemberService memberService;



    /*사용자 신고*/
    public void report(ReportMemDto reportMemDto, Authentication authentication){

        //신고 한 유저
        String userId = authentication.getName();
        Member reporter = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));
        reportMemDto.setReporter(reporter);

        //신고 당한 유저
        Member reportedMem = memberService.findMemberByName(reportMemDto.getReportedMember().getName())
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));
        reportMemDto.setReportedMember(reportedMem);


        //자기 자신을 신고한 경우
        if(reporter.getId().equals(reportedMem.getId())){
            throw new CommunityException(SELF_REPORT_NOT_ALLOWED, SELF_REPORT_NOT_ALLOWED.getCode(), SELF_REPORT_NOT_ALLOWED.getErrorMessage());
        }

        //이미 신고한 유저일 경우
        if(reportMemRepository.findByReporterAndReportedMember(reporter, reportedMem).isPresent()){
            throw new CommunityException(ALREADY_REPORTED_USER, ALREADY_REPORTED_USER.getCode(), ALREADY_REPORTED_USER.getErrorMessage());
        }

        //신고 사유가 null인 경우
        if(reportMemDto.getReason() == null){
            throw new CommunityException(REPORT_REASON_EMPTY, REPORT_REASON_EMPTY.getCode(), REPORT_REASON_EMPTY.getErrorMessage());
        }


        save(new ReportMem(reportMemDto));
    }


    public void save(ReportMem reportMem) {
        reportMemRepository.save(reportMem);
    }
}
