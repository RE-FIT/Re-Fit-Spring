package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ClosetRepository extends JpaRepository<Clothe, Long> {

    List<Clothe> findAllByCategoryAndSeasonAndMemberOrderByCountDesc(Integer category, Integer season, Member member);


    List<Clothe> findAllByCategoryAndSeasonAndMemberOrderByCountAsc(Integer category, Integer season, Member member);

    @Query("select Count(c) from Clothe c where c.category = :category and c.lastDate = :today and c.member = :member")
    int getCountOneCategoryPerOnDay(int category, LocalDate today, Member member);

    List<Clothe> findAllByCategoryAndSeasonAndMember(Integer category, Integer season, Member member);


}
