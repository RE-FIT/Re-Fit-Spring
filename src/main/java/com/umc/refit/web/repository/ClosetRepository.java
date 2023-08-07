package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Clothe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ClosetRepository extends JpaRepository<Clothe, Long> {

    List<Clothe> findAllByCategoryAndSeasonOrderByCountDesc(int category, int season);

    List<Clothe> findAllByCategoryAndSeasonOrderByCountAsc(int category, int season);

    @Query("select Count(c) from Clothe c where c.category = :category and c.lastDate = :today")
    int getCountOneCategoryPerOnDay(int category, LocalDate today);

    List<Clothe> findAllByCategoryAndSeason(Integer category, Integer season);


}
