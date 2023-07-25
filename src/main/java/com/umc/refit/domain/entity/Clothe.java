package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.clothe.GetClotheListResponseDto;
import com.umc.refit.domain.dto.clothe.GetClotheResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Date;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Clothe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closet_id")
    private Long id;

    @Column(name = "category", nullable = false)
    private Integer category;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "target_cnt") // 목표 착용 횟수
    private Integer targetCnt;

    @Column(name = "target_period") // 목표 착용 개월
    private Integer targetPeriod;

    @Column(name = "is_plan")
    private boolean isPlan;

    @Column(name = "cnt")
    private int count;

    @Column(name = "edit_cnt")
    private int editCnt;

    @Column(name = "cnt_per_month")
    private Integer cntPerMonth;

    @Column(name = "cnt_per_week")
    private Integer cntPerWeek;

    @Column(name = "last_dt")
    private Date lastDate;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public GetClotheListResponseDto from(Integer remainedDay) {
        return GetClotheListResponseDto.builder()
                .id(id)
                .imageUrl(imageUrl)
                .targetCnt(targetCnt)
                .count(count)
                .cntPerMonth(cntPerMonth)
                .cntPerWeek(cntPerWeek)
                .remainedDay(remainedDay)
                .build();
    }

    public GetClotheResponseDto toResponseDto() {
        return GetClotheResponseDto.builder()
                .id(id)
                .category(category)
                .season(season)
                .targetCnt(targetCnt)
                .targetPeriod(targetPeriod)
                .isPlan(isPlan)
                .cntPerMonth(cntPerMonth)
                .cntPerWeek(cntPerWeek)
                .imageUrl(imageUrl)
                .build();
    }
}

