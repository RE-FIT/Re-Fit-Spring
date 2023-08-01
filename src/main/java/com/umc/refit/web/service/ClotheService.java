package com.umc.refit.web.service;

import com.umc.refit.domain.dto.clothe.*;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.clothe.ClotheException;
import com.umc.refit.web.repository.ClosetRepository;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.umc.refit.exception.ExceptionType.CLOTHE_EMPTY;
import static com.umc.refit.exception.ExceptionType.ONE_CATEGORY_OVER_TWO_COUNT;


@Slf4j
@RequiredArgsConstructor
@Service
public class ClotheService {

    private final ClosetRepository closetRepository;

    private final MemberRepository memberRepository;

    private final S3UploadService s3UploadService;
    private final String bucketDirName = "closet";
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public Long registerClothe(RegisterClotheRequestDto request,
                               MultipartFile multipartFile,
                               Authentication authentication) {

        Member member = this.memberRepository.findByLoginId(
                        authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"));

        ImageDto imageDto;
        try {
            imageDto = this.s3UploadService.uploadFile(multipartFile, bucketName, bucketDirName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.closetRepository.save(request.toEntity(member, imageDto))
                .getId();
    }

    @Transactional(readOnly = true)
    public List<GetClotheListResponseDto> showClotheMain(Integer category, Integer season, String sort) {
        if (sort.equals("d-day")) {
            return null;
        } else if (sort.equals("most_worn")) {
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
        if (this.closetRepository.getCountOneCategoryPerOnDay(clothe.getCategory(), clothe.getLastDate()) >= 2) {
            throw new ClotheException(
                    ONE_CATEGORY_OVER_TWO_COUNT, ONE_CATEGORY_OVER_TWO_COUNT.getCode(), ONE_CATEGORY_OVER_TWO_COUNT.getErrorMessage());
        }
        clothe.wearClothe();
    }

    @Transactional(readOnly = true)
    public GetClotheForestResponseDto getClotheForest(Long id) {
        return getClothe(id).toGetClotheForestResponseDto();
    }

    private Clothe getClothe(Long id) {
        return this.closetRepository.findById(id)
                .orElseThrow(() -> new ClotheException(CLOTHE_EMPTY, CLOTHE_EMPTY.getCode(), CLOTHE_EMPTY.getErrorMessage()));
    }

    // 목표 미설정(is plan == false) -> -7777
    // 목표 달성(closet.getCount() >= closet.getTargetCnt()) -> +7777
    // else(목표 미달성) -> 남은 or 지난 기간
    private int calculateRemainedDay(Clothe clothe) {
        if (!clothe.isPlan()) {
            return -7777;
        }
        if (clothe.getCount() >= clothe.getTargetCnt()) {
            return 7777;
        }
        LocalDateTime targetAt = clothe.getCreatedAt().plusDays(clothe.getTargetPeriod() * 30);
        return (int) targetAt.until(LocalDateTime.now(), ChronoUnit.DAYS);
        // 기간이 남아있다면 -
        // 기간이 지났다면 +
    }
}
