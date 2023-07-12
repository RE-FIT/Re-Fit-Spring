package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Posts, Long> {
    List<Posts> findByPostTypeAndGenderAndCategoryAndRegion(Integer postType, Integer gender, Integer category, String region);
    List<Posts> findByPostTypeAndGenderAndCategory(Integer postType, Integer gender, Integer category);
}
