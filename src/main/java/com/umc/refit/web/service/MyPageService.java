package com.umc.refit.web.service;

import com.umc.refit.domain.dto.mypage.GetMyInfoResponseDto;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public GetMyInfoResponseDto getMyInfo(Authentication authentication) {
        return GetMyInfoResponseDto.from(this.memberRepository.findByLoginId(
                        authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id")));
    }

    @Transactional(readOnly = true)
    public Boolean checkInsertedName(String name) {
        return this.memberRepository.findByName(name).isPresent();
    }
}
