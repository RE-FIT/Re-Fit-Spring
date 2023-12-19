package com.umc.refit.web.repository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.refit.domain.entity.Posts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import static com.umc.refit.domain.entity.QPosts.posts;

@Repository
public class CommunityRepositoryImpl implements CommunityRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Autowired
    public CommunityRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Posts> searchPosts(Integer postType, Integer gender, Integer category, String keyword) {
        BooleanBuilder builder = new BooleanBuilder();

        // postState는 1 또는 0으로 고정
        builder.and(posts.postState.in(0, 1));

        if (postType != null) {
            builder.and(posts.postType.eq(postType));
        }
        if (gender != null) {
            builder.and(posts.gender.eq(gender));
        }
        if (category != null) {
            builder.and(posts.category.eq(category));
        }

        if (keyword != null && !keyword.isEmpty()) {
            builder.and(posts.title.toLowerCase().like("%" + keyword.toLowerCase() + "%"));
        }

        return queryFactory.selectFrom(posts)
                .where(builder)
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Posts> communityMainPage(Integer postType, Integer gender, Integer category) {
        BooleanBuilder builder = new BooleanBuilder();

        // postState는 1 또는 0으로 고정
        builder.and(posts.postState.in(0, 1));

        if (postType != null) {
            builder.and(posts.postType.eq(postType));
        }
        if (gender != null) {
            builder.and(posts.gender.eq(gender));
        }
        if (category != null) {
            builder.and(posts.category.eq(category));
        }

        return queryFactory.selectFrom(posts)
                .where(builder)
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

}
