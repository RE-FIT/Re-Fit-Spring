package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.BlockDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    /*사용자 차단 API*/
    @PostMapping
    public void block(
            @RequestBody BlockDto blockDto, Authentication authentication, HttpServletRequest request) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        blockService.block(blockDto, authentication);
    }

}
