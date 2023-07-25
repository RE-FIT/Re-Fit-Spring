package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Clothe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClosetRepository extends JpaRepository<Clothe, Long> {
    List<Clothe> findAllByOrderByCountDesc();

    List<Clothe> findAllByOrderByCountAsc();
}
