package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.clothe.RegisterClotheRequestDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.ClosetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/refit/clothe")
@RestController
public class ClotheController {

    private final ClosetService closetService;

    @PostMapping
    public ResponseEntity<Long> registerClothe(
            @RequestPart(value = "image", required = false) MultipartFile multipartFile,
            Authentication authentication,
            HttpServletRequest request,
            @Valid @RequestBody RegisterClotheRequestDto requestDto
    ) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
        return new ResponseEntity<>(this.closetService.registerClothe(requestDto, multipartFile, authentication), HttpStatus.CREATED);
    }
}
