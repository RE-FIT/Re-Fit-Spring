package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Posts, Long> {

    List<Posts> findByMemberAndPostTypeOrderByCreatedAtDesc(Member member, Integer postType);

    List<Posts> findByBuyerOrderByCreatedAtDesc(Member buyer);

}
