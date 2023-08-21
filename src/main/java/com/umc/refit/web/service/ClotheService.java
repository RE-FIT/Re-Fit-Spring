package com.umc.refit.web.service;

import com.umc.refit.domain.dto.clothe.*;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Clothe;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.clothe.ClotheException;
import com.umc.refit.web.repository.ClosetRepository;
import com.umc.refit.web.repository.MemberRepository;
import com.umc.refit.web.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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

        imageDto = this.s3UploadService.uploadFile(multipartFile, bucketName, bucketDirName);

        return this.closetRepository.save(request.toEntity(getMember(authentication), imageDto))
                .getId();
    }

    @Transactional(readOnly = true)
    public List<GetClotheListResponseDto> showClotheMain(Integer category, Integer season,
                                                         String sort,
                                                         Authentication authentication) {

        Member member = getMember(authentication);

        if (sort.equals("d-day")) {
            List<Clothe> clothes = this.closetRepository.findAllByCategoryAndSeasonAndMember(category, season, member);

            if (clothes.size() == 0) {
                return new ArrayList<>();
            }

            List<Clothe> sortedClothes = sortClothes(clothes);


            return sortClothes(sortedClothes).stream()
                    .map(clothe -> clothe.from(this.calculateRemainedDay(clothe)))
                    .collect(Collectors.toList());


        } else if (sort.equals("most-worn")) {
            return this.closetRepository.findAllByCategoryAndSeasonAndMemberOrderByCountDesc(category, season, member)
                    .stream()
                    .map(clothe -> clothe.from(this.calculateRemainedDay(clothe)))
                    .collect(Collectors.toList());
        } else {
            return this.closetRepository.findAllByCategoryAndSeasonAndMemberOrderByCountAsc(category, season, member)
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
    public void wearClothe(Long id, Authentication authentication) {
        Clothe clothe = getClothe(id);
        Member member = getMember(authentication);
        if (this.closetRepository.getCountOneCategoryPerOnDay(clothe.getCategory(), LocalDate.now(), member) >= 2) {
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

    private List<Clothe> sortClothes(List<Clothe> clothes) {
        Comparator<Object> comparator = Comparator
                .comparingInt(clothe -> {
                    int remainedDay = calculateRemainedDay((Clothe) clothe);
                    if (remainedDay > 0 && remainedDay != 7777) {
                        return -remainedDay; // 양수이면서 7777이 아닌 경우, 내림차순으로 정렬
                    } else if (remainedDay < 0 && remainedDay != -7777) {
                        return -remainedDay; // 음수이면서 -7777이 아닌 경우, 내림차순으로 정렬
                    } else {
                        return remainedDay; // 나머지는 오름차순으로 정렬
                    }
                })
                .thenComparing((clothe1, clothe2) -> {
                    int remainedDay1 = calculateRemainedDay((Clothe) clothe1);
                    int remainedDay2 = calculateRemainedDay((Clothe) clothe2);

                    if (remainedDay1 == -7777 && remainedDay2 == -7777) {
                        return -((Clothe) clothe1).getCreatedAt().compareTo(((Clothe) clothe2).getCreatedAt()); // -7777인 값들은 getCreatedAt 기준 내림차순
                    } else if (remainedDay1 == 7777 && remainedDay2 == 7777) {
                        return -((Clothe) clothe1).getCreatedAt().compareTo(((Clothe) clothe2).getCreatedAt()); // +7777인 값들은 getCreatedAt 기준 내림차순
                    } else {
                        return 0;
                    }
                });

        clothes.sort(comparator);
        return checkSorting(clothes);
    }

    private List<Clothe> checkSorting(List<Clothe> clothes) {

        ArrayList<Clothe> resultClothes = new ArrayList<>();
        ArrayList<Clothe> clothesWithNoGoal = new ArrayList<>();
        ArrayList<Clothe> clothesToRemove = new ArrayList<>();

        for (Clothe clothe : clothes) {
            if (checkIfHasNotGoal(clothe)) {
                clothesWithNoGoal.add(clothe); // 미설정 clothes list 추가
            } else {
                resultClothes.add(clothe);
            }
        }

        List<Clothe> sortedClothes = clothesWithNoGoal.stream()
                .sorted(Comparator.comparing(Clothe::getCreatedAt).reversed())
                .toList();

        resultClothes.addAll(clothesWithNoGoal);
        return resultClothes;
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
    private Integer calculateRemainedDay(Clothe clothe) {
        if (checkIfHasNotGoal(clothe)) {
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

    private boolean checkIfHasNotGoal(Clothe clothe) {
        return (!clothe.isPlan()) && (clothe.getTargetCnt() == null) && (clothe.getTargetPeriod() == null)
                && (clothe.getCntPerMonth() == null) && (clothe.getCntPerWeek() == null) && (clothe.getCompletedDate() == null);
    }

    private long getCount() {
        return this.questionRepository.count();
    }


    private Member getMember(Authentication authentication) {
        return this.memberRepository.findByLoginId(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"));
    }
}
