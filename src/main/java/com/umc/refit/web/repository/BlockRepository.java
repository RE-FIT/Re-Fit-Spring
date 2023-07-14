package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Block;
import com.umc.refit.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByRequestMemberAndBlockedMember(Member requestMem, Member blockedMem);

    List<Block> findByRequestMember(Member requestMem);
}
