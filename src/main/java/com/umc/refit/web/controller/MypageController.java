package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.PostMainResponseDto;
import com.umc.refit.domain.dto.mypage.GetMyInfoResponseDto;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.CommunityService;
import com.umc.refit.web.service.MyPageService;
import com.umc.refit.web.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/refit/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final CommunityService communityService;
    private final ScrapService scrapService;
    private final MyPageService myPageService;

    Integer give = 0;
    Integer sell = 1;

    private static void checkAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
    }

    /*내 피드 - 나눔 API*/
    @GetMapping("/myfeed/give")
    public List<PostMainResponseDto> myFeedGvie(
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        List<PostMainResponseDto> postList = communityService.myFeedPosts(give, authentication);

        return postList;
    }

    /*내 피드 - 판매 API*/
    @GetMapping("/myfeed/sell")
    public List<PostMainResponseDto> myFeedSell(
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        List<PostMainResponseDto> postList = communityService.myFeedPosts(sell, authentication);

        return postList;
    }

    /*내 피드 - 구매 API*/
    @GetMapping("/myfeed/buy")
    public List<PostMainResponseDto> myFeedBuy(
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        List<PostMainResponseDto> postList = communityService.myFeedBuy(authentication);

        return postList;
    }

    /*스크랩 - 나눔 API*/
    @GetMapping("/scrap/give")
    public List<PostMainResponseDto> myScrapGive(
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        List<PostMainResponseDto> postList = scrapService.findMyScraps(give, authentication);

        return postList;
    }

    /*스크랩 - 판매 API*/
    @GetMapping("/scrap/sell")
    public List<PostMainResponseDto> myScrapSell(
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        List<PostMainResponseDto> postList = scrapService.findMyScraps(sell, authentication);

        return postList;
    }

    /* 내 정보 조회 API */
    @GetMapping("/info")
    public ResponseEntity<GetMyInfoResponseDto> getMyInfo(
            Authentication authentication, HttpServletRequest request
    ) {
        checkAuthentication(authentication, request);
        return new ResponseEntity<>(this.myPageService.getMyInfo(authentication), HttpStatus.OK);
    }
}
