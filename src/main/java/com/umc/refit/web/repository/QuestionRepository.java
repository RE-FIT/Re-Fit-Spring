package com.umc.refit.web.repository;

import com.umc.refit.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
