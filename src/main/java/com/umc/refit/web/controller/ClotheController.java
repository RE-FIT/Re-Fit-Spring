package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.clothe.GetClotheListResponseDto;
import com.umc.refit.domain.dto.clothe.GetClotheResponseDto;
import com.umc.refit.domain.dto.clothe.RegisterClotheRequestDto;
import com.umc.refit.domain.dto.clothe.UpdateClotheRequestDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.ClotheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/refit/clothe")
@RestController
public class ClotheController {

    private final ClotheService clotheService;

    // 옷장 등록
    @PostMapping
    public ResponseEntity<Long> registerClothe(
            @RequestPart(value = "image") MultipartFile multipartFile,
            @Valid @RequestPart(value = "request") RegisterClotheRequestDto requestDto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
        return new ResponseEntity<>(this.clotheService.registerClothe(requestDto, multipartFile, authentication), HttpStatus.CREATED);
    }

    // 옷장 전체 조회
    @GetMapping
    public ResponseEntity<List<GetClotheListResponseDto>> showClotheMain(
            @RequestParam Integer category,
            @RequestParam Integer season,
            @RequestParam String sort) {
        return new ResponseEntity<>(
                this.clotheService.showClotheMain(category, season, sort), HttpStatus.OK);
    }

    // 옷장 세부 조회
    @GetMapping("/{id}")
    public ResponseEntity<GetClotheResponseDto> getClotheDetail(
            @PathVariable Long id
    ) {
        return new ResponseEntity<>(
                this.clotheService.getClotheDetail(id), HttpStatus.OK
        );
    }

    // 옷장 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClothe(
            @PathVariable Long id
    ) {
        this.clotheService.deleteClothe(id);
        return ResponseEntity.ok().build();
    }

    // 옷장 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClothe(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClotheRequestDto request
    ) {
        this.clotheService.updateClothe(id, request);
        return ResponseEntity.ok().build();
    }
}
