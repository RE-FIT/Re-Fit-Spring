package com.umc.refit.web.controller;


import com.umc.refit.domain.dto.chat.TradeRequestDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/refit/trade")
@RequiredArgsConstructor
public class TradeController {

    private final CommunityService communityService;

    /*거래 완료 API*/
    @PostMapping
    public void tradeComplete(
            @RequestBody TradeRequestDto tradeRequestDto,
            Authentication authentication, HttpServletRequest request){

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        communityService.trade(tradeRequestDto, authentication);
    }
}
