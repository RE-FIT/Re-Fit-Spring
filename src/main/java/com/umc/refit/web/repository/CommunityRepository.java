package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Posts, Long> {

}
