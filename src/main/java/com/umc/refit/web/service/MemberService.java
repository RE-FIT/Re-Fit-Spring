package com.umc.refit.web.service;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Member> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findMemberForPasswordRest(String name, String email, String loginId) {
        return memberRepository.findMemberByNameAndEmailAndLoginId(name, email, loginId);
    }

    public Optional<Member> findMemberForFindId(String name, String email) {
        return memberRepository.findMemberByNameAndEmail(name, email);
    }

    public Optional<Member> findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId);
    }

    public Optional<Member> findMemberByName(String name) {
        return memberRepository.findByName(name);
    }

    /*패스워드 인코딩 후 저장*/
    public void save(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }
}
