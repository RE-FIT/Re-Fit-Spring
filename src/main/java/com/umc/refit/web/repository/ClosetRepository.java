package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Clothe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClosetRepository extends JpaRepository<Clothe, Long> {
    // 가장 많이 입은 순 :
    List<Clothe> findAllByOrderByCountDesc();

    List<Clothe> findAllByOrderByCountAsc();

    @Query("select c from Clothe c")
    List<Clothe> findAllByDDayDesc();
}
