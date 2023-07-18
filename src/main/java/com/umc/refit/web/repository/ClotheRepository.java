package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Closet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClotheRepository extends JpaRepository<Closet, Long> {
}
