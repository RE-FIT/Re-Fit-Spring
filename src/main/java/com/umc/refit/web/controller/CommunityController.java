package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.*;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.CommunityService;
import com.umc.refit.web.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.umc.refit.exception.ExceptionType.*;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    private final ScrapService scrapService;


    /*커뮤니티 메인 화면 API*/
    @GetMapping
    public List<PostMainResponseDto> communityMain(
            @RequestParam(value = "postType", defaultValue = "0") Integer postType,
            @RequestParam(value = "gender", defaultValue = "0") Integer gender,
            @RequestParam(value = "category", defaultValue = "0") Integer category,
            @RequestParam(value = "region", defaultValue = "서울") String region,
            Authentication authentication, HttpServletRequest request){

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        /*카테고리 선택에 맞는 게시글 리스트*/
        List<PostMainResponseDto> postList = null;
        /*선택된 카테고리가 나눔일 경우*/
        if (postType == 0){
            postList = communityService.communityShareMain(postType, gender, category, authentication);
        } else if (postType == 1) {
            /*선택된 카테고리가 판매일 경우*/
            communityService.communitySellMain(postType, gender, category, region, authentication);
        }
        return postList;
    }


    /*게시글 조회 API*/
    @GetMapping("/{postId}")
    public PostClickResponseDto clickPost(@PathVariable Long postId,
                                          Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        PostClickResponseDto post = communityService.clickPost(postId, authentication);

        return post;
    }


    /*글 등록 API*/
     @PostMapping
     public void post(
     @RequestPart(value="image", required = false) List<MultipartFile> multipartFiles,
     @RequestPart PostDto postDto, Authentication authentication, HttpServletRequest request) throws IOException{

         if (authentication == null) {
             ExceptionType exception = (ExceptionType) request.getAttribute("exception");
             throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
         }

         /*
          * 1. postDto 중 필수 값 없으면 예외
          * ( 멤버 제외 제목, 추천성별, 거래타입, 카테고리, 사이즈, 배송타입, 상세설명, 사진 )
          * */
         if(postDto.getTitle() == null){
             throw new CommunityException(TITLE_EMPTY, TITLE_EMPTY.getCode(), TITLE_EMPTY.getErrorMessage());
         }
         if(postDto.getGender() == null){
             throw new CommunityException(GENDER_EMPTY, GENDER_EMPTY.getCode(), GENDER_EMPTY.getErrorMessage());
         }
         //판매글일 때 price 값 안 들어왔을 때 예외처리 추가해야함
         if(postDto.getPostType() == null){
             throw new CommunityException(POST_TYPE_EMPTY, POST_TYPE_EMPTY.getCode(), POST_TYPE_EMPTY.getErrorMessage());
         }
         if(postDto.getCategory() == null){
             throw new CommunityException(CATEGORY_EMPTY, CATEGORY_EMPTY.getCode(), CATEGORY_EMPTY.getErrorMessage());
         }
         if(postDto.getSize() == null){
             throw new CommunityException(SIZE_EMPTY, SIZE_EMPTY.getCode(), SIZE_EMPTY.getErrorMessage());
         }
         //거래 방식이 택배인데 deliveryfee 값 안 들어왔을 때 예외처리 추가해야함
         if(postDto.getDeliveryType() == null){
             throw new CommunityException(DELIVERY_TYPE_EMPTY, DELIVERY_TYPE_EMPTY.getCode(), DELIVERY_TYPE_EMPTY.getErrorMessage());
         }
         if(postDto.getDetail() == null){
             throw new CommunityException(DETAIL_EMPTY, DETAIL_EMPTY.getCode(), DETAIL_EMPTY.getErrorMessage());
         }
         if(CollectionUtils.isEmpty(multipartFiles)){
             throw new CommunityException(IMAGE_EMPTY, IMAGE_EMPTY.getCode(), IMAGE_EMPTY.getErrorMessage());
         }

         /*
          * 2. multipartFile.size() <=5 아니면 예외
          * */

         //이미지 개수 5개 이하로 제한
         if(multipartFiles.size()>5){
             throw new FileCountLimitExceededException("error: file count limit exceeded", 5);
         }

        communityService.create(postDto, multipartFiles, authentication);
     }

     /*게시글 삭제 API*/
    @DeleteMapping("/{postId}")
    public void deletePost(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        communityService.deletePost(postId, authentication);
    }

    /*게시글 상태 변경 API*/
    @PatchMapping("/{postId}")
    public void changeState(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        communityService.changeState(postId, authentication);
    }


    /*게시글 스크랩 API*/
    @PostMapping("/{postId}/scrap")
    public void scrap(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        scrapService.scrap(postId, authentication);
    }

}
