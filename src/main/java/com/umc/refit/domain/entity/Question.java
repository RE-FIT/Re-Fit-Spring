package com.umc.refit.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int category;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private boolean answer;

    @Column(nullable = false)
    private String explanation;

    public Question(int category, String question, boolean answer, String explanation) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.explanation = explanation;
    }
}
