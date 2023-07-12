package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.ReportMemDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.ReportMemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportMemController {

    private final ReportMemService reportMemService;


    /*사용자 신고 API*/
    @PostMapping
    public void report(
            @RequestBody ReportMemDto reportMemDto, Authentication authentication, HttpServletRequest request) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        if (reportMemDto.getReason() == null) {
            throw new IllegalStateException("신고 사유 선택은 필수입니다.");
        }

        reportMemService.report(reportMemDto, authentication);
    }
}
