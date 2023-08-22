package com.umc.refit.domain.dto.clothe;

import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.domain.entity.Question;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetClotheForestResponseDto {
    private Long id;
    private String imageUrl;
    private int targetCnt;
    private int count;
    private int remainedCnt;
    private List<GetClotheForestRandomQuestionResponseDto> questions;

    public static GetClotheForestResponseDto of(Clothe clothe, List<Question> randomQuestions) {
        return GetClotheForestResponseDto.builder()
                .id(clothe.getId())
                .imageUrl(clothe.getImageUrl())
                .targetCnt(clothe.getTargetCnt())
                .count(clothe.getCount())
                .remainedCnt(clothe.getTargetCnt() - clothe.getCount())
                .questions(
                        randomQuestions.stream()
                                .map(GetClotheForestRandomQuestionResponseDto::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    static class GetClotheForestRandomQuestionResponseDto {

        private Long id;

        private int category;

        private String question;

        private boolean answer;

        private String explanation;

        public static GetClotheForestRandomQuestionResponseDto from(Question question) {
            return GetClotheForestRandomQuestionResponseDto.builder()
                    .id(question.getId())
                    .category(question.getCategory())
                    .question(question.getQuestion())
                    .answer(question.isAnswer())
                    .explanation(question.getExplanation())
                    .build();
        }
    }
}
