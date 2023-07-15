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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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


    /*게시글 등록 API*/
     @PostMapping
     public PostClickResponseDto post(
             @RequestPart(value="image", required = false) List<MultipartFile> multipartFiles,
             @Valid @RequestPart PostDto postDto,
             BindingResult bindingResult,
             Authentication authentication, HttpServletRequest request) throws IOException{

         if (authentication == null) {
             ExceptionType exception = (ExceptionType) request.getAttribute("exception");
             throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
         }

         if (bindingResult.hasErrors()) {
             String errors = bindingResult.getFieldErrors().stream()
                     .map(FieldError::getDefaultMessage)
                     .collect(Collectors.joining(", "));
             throw new IllegalArgumentException(errors);
         }

         checkException(postDto, multipartFiles);

         PostClickResponseDto post = communityService.create(postDto, multipartFiles, authentication);

         return post;
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
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        scrapService.scrap(postId, authentication);
    }

    /*게시글 검색*/
    @GetMapping("/search")
    public List<PostMainResponseDto> search(
            @RequestParam String keyword,
            Authentication authentication, HttpServletRequest request) {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        return communityService.searchPosts(keyword);
    }

    /*게시글 수정*/
    @PutMapping("/{postId}/update")
    public PostClickResponseDto update(
            @PathVariable Long postId,
            @RequestPart(value="image", required = false) List<MultipartFile> multipartFiles,
            @Valid @RequestPart PostDto postDto,
            BindingResult bindingResult, Authentication authentication, HttpServletRequest request) throws IOException {

        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(errors);
        }

        checkException(postDto, multipartFiles);

        PostClickResponseDto post = communityService.update(postId, postDto, multipartFiles, authentication);

        return post;
    }

    public void checkException(PostDto postDto, List<MultipartFile> multipartFiles) throws FileCountLimitExceededException {
        //판매글인데 가격이 null이면 에러
        if(postDto.getPostType()==1 && postDto.getPrice() == null){
            throw new CommunityException(PRICE_EMPTY, PRICE_EMPTY.getCode(), PRICE_EMPTY.getErrorMessage());
        }
        //거래 방식이 택배 배송인데 배송비가 null이면 에러
        if(postDto.getDeliveryType()==1 && postDto.getDeliveryFee()==null){
            throw new CommunityException(DELIVERY_FEE_EMPTY, DELIVERY_FEE_EMPTY.getCode(), DELIVERY_FEE_EMPTY.getErrorMessage());
        }
        //거래 방식이 직거래인데 거래 희망 지역이 없으면 에러
        if (postDto.getDeliveryType() == 0 && postDto.getRegion() == null){
            throw new CommunityException(REGION_EMPTY, REGION_EMPTY.getCode(), REGION_EMPTY.getErrorMessage());
        }
        //사진이 하나도 없으면 예외
        if(CollectionUtils.isEmpty(multipartFiles)){
            throw new CommunityException(IMAGE_EMPTY, IMAGE_EMPTY.getCode(), IMAGE_EMPTY.getErrorMessage());
        }
        //이미지 개수 5개 이하로 제한
        if(multipartFiles.size()>5){
            throw new FileCountLimitExceededException("error: file count limit exceeded", 5);
        }
    }

}
