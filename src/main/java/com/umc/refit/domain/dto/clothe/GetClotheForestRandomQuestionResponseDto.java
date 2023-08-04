package com.umc.refit.domain.dto.clothe;

import com.umc.refit.domain.entity.Question;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class GetClotheForestRandomQuestionResponseDto {
    private int category;

    private String question;

    private boolean answer;

    private String explanation;

    public static GetClotheForestRandomQuestionResponseDto from(Question question) {
        return GetClotheForestRandomQuestionResponseDto.builder()
                .category(question.getCategory())
                .question(question.getQuestion())
                .answer(question.isAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}
