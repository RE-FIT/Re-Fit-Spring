package com.umc.refit.web.service;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Optional<Member> findMember(String email) {
        return memberRepository.findByEmail(email);
    }
}
