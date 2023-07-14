package com.umc.refit.web.service;

import com.umc.refit.domain.dto.clothe.RegisterClotheRequestDto;
import com.umc.refit.web.repository.ClotheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClotheService {

    private final ClotheRepository clotheRepository;

    @Transactional
    public Long registerClothe(RegisterClotheRequestDto request) {
        return this.clotheRepository.save(request.toEntity())
                .getId();
    }
}
