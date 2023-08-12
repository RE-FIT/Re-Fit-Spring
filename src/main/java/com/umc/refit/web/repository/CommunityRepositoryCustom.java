package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Posts;

import java.util.List;

public interface CommunityRepositoryCustom {
    List<Posts> searchPosts(Integer postType, Integer gender, Integer category, String keyword);

    List<Posts> communityMainPage(Integer postType, Integer gender, Integer category);
}
