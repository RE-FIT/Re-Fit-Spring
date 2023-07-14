package com.umc.refit.domain.entity;

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
    @Column(name = "clothe_id")
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}

