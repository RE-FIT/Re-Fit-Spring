package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Posts, Long> {
    List<Posts> findByPostTypeAndGenderAndCategoryAndRegion(Integer postType, Integer gender, Integer category, String region);
    List<Posts> findByPostTypeAndGenderAndCategory(Integer postType, Integer gender, Integer category);

    @Query("SELECT p FROM Posts p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Posts> findByTitleContainingIgnoreCaseCustom(@Param("keyword") String keyword);

}
