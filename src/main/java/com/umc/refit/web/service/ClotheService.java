package com.umc.refit.web.service;

import com.umc.refit.domain.dto.clothe.*;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.exception.clothe.ClotheException;
import com.umc.refit.web.repository.ClosetRepository;
import com.umc.refit.web.repository.MemberRepository;
import com.umc.refit.web.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.umc.refit.exception.ExceptionType.CLOTHE_EMPTY;
import static com.umc.refit.exception.ExceptionType.ONE_CATEGORY_OVER_TWO_COUNT;


@Slf4j
@RequiredArgsConstructor
@Service
public class ClotheService {

    private final ClosetRepository closetRepository;

    private final MemberRepository memberRepository;

    private final QuestionRepository questionRepository;

    private final S3UploadService s3UploadService;
    private final String bucketDirName = "closet";

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public Long registerClothe(RegisterClotheRequestDto request,
                               MultipartFile multipartFile,
                               Authentication authentication) {

        ImageDto imageDto;
        try {
            imageDto = this.s3UploadService.uploadFile(multipartFile, bucketName, bucketDirName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.closetRepository.save(request.toEntity(this.memberRepository.findByLoginId(authentication.getName())
                        .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id")), imageDto))
                .getId();
    }

    @Transactional(readOnly = true)
    public List<GetClotheListResponseDto> showClotheMain(ShowClotheMainRequestDto request) {
        if (request.getSort().equals("d-day")) {

            List<Clothe> clothes
                    = this.closetRepository.findAll(PageRequest.of(request.getPage(), request.getSize()))
                    .getContent();

            clothes = new ArrayList<>(clothes);

            if (clothes.isEmpty()) {
                return null;
            }

            clothes.sort((c1, c2) -> {
                int remainedDay1 = calculateRemainedDay(c1);
                int remainedDay2 = calculateRemainedDay(c2);

                boolean isPositive1 = remainedDay1 >= 0 && remainedDay1 != 7777;
                boolean isPositive2 = remainedDay2 >= 0 && remainedDay2 != 7777;
                boolean isNegative1 = remainedDay1 < 0 && remainedDay1 != -7777;
                boolean isNegative2 = remainedDay2 < 0 && remainedDay2 != -7777;

                if (isPositive1 && isPositive2) {
                    // 양수인 값들은 내림차순 정렬
                    return Integer.compare(remainedDay2, remainedDay1);
                } else if (isNegative1 && isNegative2) {
                    // 음수인 값들은 오름차순 정렬
                    return Integer.compare(remainedDay1, remainedDay2);
                } else if (isNegative1) {
                    // -7777 인 값들은 lastDate 기준으로 내림차순 정렬
                    return c2.getLastDate().compareTo(c1.getLastDate());
                } else if (isNegative2) {
                    // -7777 인 값들은 lastDate 기준으로 내림차순 정렬
                    return c2.getLastDate().compareTo(c1.getLastDate());
                } else if (isPositive1) {
                    // +7777 인 값들은 completedDate 기준으로 내림차순 정렬
                    return c2.getCompletedDate().compareTo(c1.getCompletedDate());
                } else {
                    // +7777 인 값들은 completedDate 기준으로 내림차순 정렬
                    return c2.getCompletedDate().compareTo(c1.getCompletedDate());
                }
            });

            return clothes.stream()
                    .map(clothe -> clothe.from(this.calculateRemainedDay(clothe)))
                    .collect(Collectors.toList());

        } else if (request.getSort().equals("most_worn")) {
            return this.closetRepository.findAllByOrderByCountDesc()
                    .stream()
                    .map(clothe -> clothe.from(this.calculateRemainedDay(clothe)))
                    .collect(Collectors.toList());
        } else {
            return this.closetRepository.findAllByOrderByCountAsc()
                    .stream()
                    .map(clothe -> clothe.from(this.calculateRemainedDay(clothe)))
                    .collect(Collectors.toList());
        }
    }


    @Transactional(readOnly = true)
    public GetClotheResponseDto getClotheDetail(Long id) {
        return getClothe(id).toResponseDto();
    }

    @Transactional
    public void deleteClothe(Long id) {
        Clothe clothe = getClothe(id);
        this.closetRepository.delete(clothe);
    }

    @Transactional
    public void updateClothe(Long id, UpdateClotheRequestDto request) {
        getClothe(id).update(request);
    }


    @Transactional
    public void updateClotheGoal(Long id, UpdateClotheGoalRequestDto request) {
        Clothe clothe = getClothe(id);
        clothe.updateGoal(request);
    }

    @Transactional
    public void wearClothe(Long id) {
        Clothe clothe = getClothe(id);
        if (this.closetRepository.getCountOneCategoryPerOnDay(clothe.getCategory(), LocalDate.now()) >= 2) {
            throw new ClotheException(
                    ONE_CATEGORY_OVER_TWO_COUNT, ONE_CATEGORY_OVER_TWO_COUNT.getCode(), ONE_CATEGORY_OVER_TWO_COUNT.getErrorMessage());
        }
        clothe.wearClothe();
    }

    @Transactional(readOnly = true)
    public GetClotheForestResponseDto getClotheForest(Long id) {
        return getClothe(id).toClotheForestResponseDto();
    }

    @Transactional(readOnly = true)
    public GetClotheForestRandomQuestionResponseDto getClotheForestQuestion(Long id) {
        getClothe(id);
        int randomIndex = new Random().nextInt((int) getCount());
        return GetClotheForestRandomQuestionResponseDto
                .from(this.questionRepository.findAll().get(randomIndex));
    }

    private Clothe getClothe(Long id) {
        return this.closetRepository.findById(id)
                .orElseThrow(() -> new ClotheException(CLOTHE_EMPTY, CLOTHE_EMPTY.getCode(), CLOTHE_EMPTY.getErrorMessage()));
    }

    // case 1. 등록한 계절과 현재 계절이 일치하는 경우 -> targetPeriod,targetCnt,cntPerMonth,cntPerWeek is not null & isPlan = false
    // case 2. 등록한 계절과 현재 계절이 일치하지 않는 경우 + 당분간 계획이 [있는] 경우 -> targetPeriod,targetCnt,cntPerMonth,cntPerWeek is not null & isPlan = true
    // case 2. 등록한 계절과 현재 계절이 일치하지 않는 경우 + 당분간 계획이 [없는] 경우 -> targetPeriod,targetCnt,cntPerMonth,cntPerWeek is null & isPlan = false


    // 목표 미설정(is plan == false) -> -7777
    // 목표 달성(closet.getCount() >= closet.getTargetCnt()) -> +7777
    // else(목표 미달성) -> 남은 or 지난 기간
    private int calculateRemainedDay(Clothe clothe) {
        if (checkHasNoPlanAndTimeIsNotSame(clothe)) {
            // 목표 미설정
            return -7777;
        }
        if (clothe.getCompletedDate() != null) {
            // 목표 달성
            return 7777;
        }
        LocalDateTime targetAt = clothe.getCreatedAt().plusDays(clothe.getTargetPeriod() * 30);
        return (int) targetAt.until(LocalDateTime.now(), ChronoUnit.DAYS);
        // 기간이 남아있다면 -
        // 기간이 지났다면 +
    }

    private boolean checkHasNoPlanAndTimeIsNotSame(Clothe clothe) {
        return (!clothe.isPlan()) && (clothe.getTargetCnt() == null) && (clothe.getTargetPeriod() == null)
                && (clothe.getCntPerMonth() == null) && (clothe.getCntPerWeek() == null);
    }

    private long getCount() {
        return this.questionRepository.count();
    }
}
