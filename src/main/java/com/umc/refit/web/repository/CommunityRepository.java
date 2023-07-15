package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Posts, Long> {
    List<Posts> findByPostTypeAndGenderAndCategoryAndRegionAndPostState(Integer postType, Integer gender, Integer category, String region, Integer postState);
    List<Posts> findByPostTypeAndGenderAndCategoryAndPostState(Integer postType, Integer gender, Integer category, Integer postState);

    @Query("SELECT p FROM Posts p WHERE (p.postState = 0 OR p.postState = 1) AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Posts> findByTitleContainingIgnoreCaseAndPostStateCustom(@Param("keyword") String keyword);

}
