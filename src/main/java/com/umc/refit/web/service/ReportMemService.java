package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.ReportMemDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.ReportMem;
import com.umc.refit.web.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;

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
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));
        reportMemDto.setReporter(reporter);

        //신고 당한 유저
        Member reportedMem = memberService.findMemberByName(reportMemDto.getReportedMember().getName())
                .orElseThrow(() -> new NoSuchElementException("No member found with this name"));
        reportMemDto.setReportedMember(reportedMem);


        //자기 자신을 신고한 경우
        if(reporter.getId().equals(reportedMem.getId())){
            throw new IllegalStateException("자기 자신은 신고 불가합니다.");
        }

        //이미 신고한 유저일 경우
        if(reportMemRepository.findByReporterAndReportedMember(reporter, reportedMem).isPresent()){
            throw new IllegalStateException("이미 신고한 유저입니다.");
        }

        //신고 사유가 '기타'일 경우
        if(reportMemDto.getReason().equals(5)){
            if(reportMemDto.getReasonDetail() == null){
                throw new IllegalStateException("기타 신고 사유 작성은 필수입니다.");
            }
        }

        save(new ReportMem(reportMemDto));
    }


    public void save(ReportMem reportMem) {
        reportMemRepository.save(reportMem);
    }
}
