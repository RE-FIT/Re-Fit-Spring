package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.PostDto;
import com.umc.refit.domain.dto.community.ReportMemDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.ReportMemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/refit/report")
@RequiredArgsConstructor
public class ReportMemController {

    private final ReportMemService reportMemService;


    /*사용자 신고 API*/
    @PostMapping
    public void report(
            @Valid @RequestBody ReportMemDto reportMemDto,
            BindingResult bindingResult,
            Authentication authentication, HttpServletRequest request) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getFieldError().getDefaultMessage());
        }

        reportMemService.report(reportMemDto, authentication);
    }
}
