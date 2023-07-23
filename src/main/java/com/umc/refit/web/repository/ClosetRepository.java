package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClosetRepository extends JpaRepository<Closet, Long> {
    List<Closet> findAllByOrderByCountDesc();

    List<Closet> findAllByOrderByCountAsc();
}
