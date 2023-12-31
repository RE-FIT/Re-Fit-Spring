package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.PostMainResponseDto;
import com.umc.refit.domain.dto.community.PostMyPageResponseDto;
import com.umc.refit.domain.dto.mypage.GetMyInfoResponseDto;
import com.umc.refit.domain.dto.mypage.GetMyResponseDto;
import com.umc.refit.domain.dto.mypage.UpdateMyInfoRequestDto;
import com.umc.refit.domain.dto.mypage.UpdatePasswordRequestDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.CommunityService;
import com.umc.refit.web.service.MemberService;
import com.umc.refit.web.service.MyInfoService;
import com.umc.refit.web.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/refit/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final CommunityService communityService;
    private final ScrapService scrapService;
    private final MyInfoService myInfoService;
    private final MemberService memberService;

    Integer give = 0;
    Integer sell = 1;

    private static void checkAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
    }

    @GetMapping
    public GetMyResponseDto myPage(Authentication authentication, HttpServletRequest request) {
        checkAuthentication(authentication, request);
        Optional<Member> findMember = memberService.findMemberByLoginId(authentication.getName());
        return new GetMyResponseDto(findMember.get());
    }

    /*내 피드 - 나눔 API*/
    @GetMapping("/myfeed/give")
    public List<PostMyPageResponseDto> myFeedGvie(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        checkAuthentication(authentication, httpServletRequest);
        return communityService.myFeedPosts(give, authentication);
    }

    /*내 피드 - 판매 API*/
    @GetMapping("/myfeed/sell")
    public List<PostMyPageResponseDto> myFeedSell(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        checkAuthentication(authentication, httpServletRequest);
        return communityService.myFeedPosts(sell, authentication);
    }

    /*내 피드 - 구매 API*/
    @GetMapping("/myfeed/buy")
    public List<PostMyPageResponseDto> myFeedBuy(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        checkAuthentication(authentication, httpServletRequest);
        return communityService.myFeedBuy(authentication);
    }

    /*스크랩 - 나눔 API*/
    @GetMapping("/scrap/give")
    public List<PostMainResponseDto> myScrapGive(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        checkAuthentication(authentication, httpServletRequest);
        return scrapService.findMyScraps(give, authentication);
    }

    /*스크랩 - 판매 API*/
    @GetMapping("/scrap/sell")
    public List<PostMainResponseDto> myScrapSell(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {

        checkAuthentication(authentication, httpServletRequest);
        return scrapService.findMyScraps(sell, authentication);
    }

    /* 내 정보 조회 API */
    @GetMapping("/info")
    public ResponseEntity<GetMyInfoResponseDto> getMyInfo(
            Authentication authentication, HttpServletRequest httpServletRequest
    ) {
        checkAuthentication(authentication, httpServletRequest);
        return new ResponseEntity<>(this.myInfoService.getMyInfo(authentication), HttpStatus.OK);
    }

    /* 내 정보 수정 API */
    @PatchMapping("/info")
    public ResponseEntity<Void> updateMyInfo(
            @RequestPart(value = "image", required = false) MultipartFile multipartFile,
            @Valid @RequestPart UpdateMyInfoRequestDto request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        checkAuthentication(authentication, httpServletRequest);
        this.myInfoService.updateMyInfo(multipartFile, request, authentication);
        return ResponseEntity.ok().build();
    }

    /* 이름(닉네임) 중복 확인 API */
    @GetMapping("/info/check")
    public ResponseEntity<Boolean> checkInsertedName(
            @RequestParam String name
    ) {
        return new ResponseEntity<>(this.myInfoService.checkInsertedName(name), HttpStatus.OK);
    }

    /* 회원 비밀 번호 수정 API */
    @PatchMapping("/info/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestDto request,
            Authentication authentication,
            HttpServletRequest httpServletRequest
    ) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) httpServletRequest.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
        this.myInfoService.updatePassword(request, authentication);
        return ResponseEntity.ok().build();
    }
}
