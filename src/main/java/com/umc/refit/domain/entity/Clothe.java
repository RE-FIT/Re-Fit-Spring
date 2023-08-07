package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.clothe.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Column(name = "target_cnt")
    private Integer targetCnt;

    @Column(name = "target_period")
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
    private LocalDate lastDate;

    @Column(name = "target_completed_dt")
    private LocalDate completedDate;

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
                .lastDate(lastDate)
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

    public GetClotheForestResponseDto toClotheForestResponseDto() {
        return GetClotheForestResponseDto.builder()
                .id(id)
                .imageUrl(imageUrl)
                .targetCnt(targetCnt)
                .count(count)
                .remainedCnt(targetCnt - count)
                .build();
    }

    public void update(UpdateClotheRequestDto request) {
        this.category = request.getCategory();
        this.season = request.getSeason();
        this.targetCnt = request.getTargetCnt();
        this.targetPeriod = request.getTargetPeriod();
        this.isPlan = request.getIsPlan();
        this.cntPerMonth = request.getCntPerMonth();
        this.cntPerWeek = request.getCntPerWeek();
        this.editCnt += 1;
    }

    public void updateGoal(UpdateClotheGoalRequestDto request) {
        this.season = request.getSeason();
        this.targetCnt = request.getTargetCnt();
        this.targetPeriod = request.getTargetPeriod();
        this.isPlan = request.getIsPlan();
        this.cntPerMonth = request.getCntPerMonth();
        this.cntPerWeek = request.getCntPerWeek();
        this.editCnt += 1;
    }

    // 이미 달성한 옷이나
    // 목표 설정을 안한 옷은 불가
    public void wearClothe() {
        this.count += 1;
        if (this.count == this.targetCnt) {
            this.completedDate = LocalDate.now();
        }
        this.lastDate = LocalDate.now();
    }
}

