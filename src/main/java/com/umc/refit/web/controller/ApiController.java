package com.umc.refit.web.controller;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ApiController {

    private final MemberRepository memberRepository;

    @GetMapping("/")
    public String hello() {
        return "hello";
    }

    @GetMapping("/save/{name}")
    public String name(@PathVariable("name") String name) {

        memberRepository.save(new Member(name));

        Optional<Member> member = memberRepository.findByName(name);

        return member.get().getName();
    }
}
