package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByName(String name);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findMemberByNameAndEmailAndLoginId(String name, String email, String loginId);

    Optional<Member> findMemberByNameAndEmail(String name, String email);

}
