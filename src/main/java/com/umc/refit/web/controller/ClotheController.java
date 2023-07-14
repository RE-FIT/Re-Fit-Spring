package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.clothe.RegisterClotheRequestDto;
import com.umc.refit.web.service.ClotheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/refit/clothe")
@RestController
public class ClotheController {

    private final ClotheService clotheService;

    @PostMapping
    public ResponseEntity<Long> registerClothe(
            @Valid @RequestBody RegisterClotheRequestDto request
    ) {
        return new ResponseEntity<>(this.clotheService.registerClothe(request), HttpStatus.CREATED);
    }
}
