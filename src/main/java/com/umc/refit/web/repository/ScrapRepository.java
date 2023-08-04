package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    Optional<Scrap> findByMemberAndPostId(Member member, Long postId);

    List<Scrap> findByMember(Member member);

    boolean existsByMemberAndPostId(Member member, Long postId);
}
