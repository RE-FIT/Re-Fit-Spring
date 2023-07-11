package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CmImgRepository extends JpaRepository<PostImage, Long> {
}
