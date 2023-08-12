package com.umc.refit.web.controller;

import com.umc.refit.domain.dto.community.*;
import com.umc.refit.exception.ExceptionType;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.exception.member.TokenException;
import com.umc.refit.web.service.CommunityService;
import com.umc.refit.web.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.umc.refit.exception.ExceptionType.*;

@RestController
@RequestMapping("/refit/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    private final ScrapService scrapService;


    private static void checkAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new TokenException(exception, exception.getCode(), exception.getErrorMessage());
        }
    }

    /*커뮤니티 메인 화면 API*/
    @GetMapping
    public List<PostMainResponseDto> communityMain(
            @RequestParam(required = false) Integer postType,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer category,
            Authentication authentication, HttpServletRequest request){

        checkAuthentication(authentication, request);

        //게시글 타입 범위 오류
        if(!(postType == null)){
            if(postType < 0 || postType > 1){
                throw new CommunityException(POST_TYPE_RANGE_ERR, POST_TYPE_RANGE_ERR.getCode(), POST_TYPE_RANGE_ERR.getErrorMessage());
            }
        }

        //추천 성별 범위 오류
        if(!(gender == null)){
            if(gender < 0 || gender > 1){
                throw new CommunityException(GENDER_RANGE_ERR, GENDER_RANGE_ERR.getCode(), GENDER_RANGE_ERR.getErrorMessage());
            }
        }

        //카테고리 범위 오류
        if(!(category == null)){
            if(category < 0 || category > 5){
                throw new CommunityException(CATEGORY_RANGE_ERR, CATEGORY_RANGE_ERR.getCode(), CATEGORY_RANGE_ERR.getErrorMessage());
            }
        }

        /*카테고리 선택에 맞는 게시글 리스트*/
        return communityService.communityMainPosts(postType, gender, category, authentication, scrapService);
    }


    /*게시글 조회 API*/
    @GetMapping("/{postId}")
    public PostClickResponseDto clickPost(@PathVariable Long postId,
                                          Authentication authentication, HttpServletRequest request) {
        checkAuthentication(authentication, request);

        return communityService.clickPost(postId, authentication, scrapService);
    }


    /*게시글 등록 API*/
     @PostMapping
     public PostClickResponseDto post(
             @RequestPart(value="image", required = false) List<MultipartFile> multipartFiles,
             @RequestPart PostDto postDto,
             Authentication authentication, HttpServletRequest request) throws IOException{

         checkAuthentication(authentication, request);

         checkPostException(postDto, multipartFiles);

         return communityService.create(postDto, multipartFiles, authentication);
     }

     /*게시글 삭제 API*/
    @DeleteMapping("/{postId}")
    public void deletePost(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        checkAuthentication(authentication, request);

        communityService.deletePost(postId, authentication);
    }

    /*게시글 상태 변경 API*/
    @PatchMapping("/{postId}")
    public PostClickResponseDto changeState(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        checkAuthentication(authentication, request);

        return communityService.changeState(postId, authentication);
    }


    /*게시글 스크랩 API*/
    @PostMapping("/{postId}/scrap")
    public void scrap(
            @PathVariable Long postId, Authentication authentication, HttpServletRequest request){

        checkAuthentication(authentication, request);

        scrapService.scrap(postId, authentication);
    }

    /*게시글 검색*/
    @GetMapping("/search")
    public List<PostMainResponseDto> search(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer postType,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer category,
            Authentication authentication, HttpServletRequest request) {

        checkAuthentication(authentication, request);

        //게시글 타입 범위 오류
        if(!(postType == null)){
            if(postType < 0 || postType > 1){
                throw new CommunityException(POST_TYPE_RANGE_ERR, POST_TYPE_RANGE_ERR.getCode(), POST_TYPE_RANGE_ERR.getErrorMessage());
            }
        }

        //추천 성별 범위 오류
        if(!(gender == null)){
            if(gender < 0 || gender > 1){
                throw new CommunityException(GENDER_RANGE_ERR, GENDER_RANGE_ERR.getCode(), GENDER_RANGE_ERR.getErrorMessage());
            }
        }

        //카테고리 범위 오류
        if(!(category == null)){
            if(category < 0 || category > 5){
                throw new CommunityException(CATEGORY_RANGE_ERR, CATEGORY_RANGE_ERR.getCode(), CATEGORY_RANGE_ERR.getErrorMessage());
            }
        }

        return communityService.searchPosts(authentication, keyword, scrapService, postType, gender, category);
    }

    /*게시글 수정*/
    @PutMapping("/{postId}/update")
    public PostClickResponseDto update(
            @PathVariable Long postId,
            @RequestPart(value="image", required = false) List<MultipartFile> multipartFiles,
            @RequestPart PostDto postDto,
            @RequestPart(value="image_updated") boolean image_updated,
            Authentication authentication, HttpServletRequest request){

        checkAuthentication(authentication, request);

        //이미지가 수정된 경우
        if (image_updated){
            checkPostException(postDto, multipartFiles);
            return communityService.update(postId, postDto, multipartFiles, authentication);
        }
        //이미지가 수정되지 않은 경우
        checkPostExceptionWithoutImage(postDto);
        return communityService.updateWithoutImage(postId, postDto, authentication);
    }


    public void checkPostException(PostDto postDto, List<MultipartFile> multipartFiles) {
        //제목 null
        if(postDto.getTitle() == null){
            throw new CommunityException(TITLE_EMPTY, TITLE_EMPTY.getCode(), TITLE_EMPTY.getErrorMessage());
        }
        //추천 성별 null
        if(postDto.getGender() == null){
            throw new CommunityException(GENDER_EMPTY, GENDER_EMPTY.getCode(), GENDER_EMPTY.getErrorMessage());
        }
        //추천 성별 범위 오류
        if(postDto.getGender() < 0 || postDto.getGender() > 1){
            throw new CommunityException(GENDER_RANGE_ERR, GENDER_RANGE_ERR.getCode(), GENDER_RANGE_ERR.getErrorMessage());
        }
        //게시글 타입 null
        if(postDto.getPostType() == null){
            throw new CommunityException(POST_TYPE_EMPTY, POST_TYPE_EMPTY.getCode(), POST_TYPE_EMPTY.getErrorMessage());
        }
        //게시글 타입 범위 오류
        if(postDto.getPostType()  < 0 || postDto.getPostType() > 1){
            throw new CommunityException(POST_TYPE_RANGE_ERR, POST_TYPE_RANGE_ERR.getCode(), POST_TYPE_RANGE_ERR.getErrorMessage());
        }
        //카테고리 null
        if(postDto.getCategory() == null){
            throw new CommunityException(CATEGORY_EMPTY, CATEGORY_EMPTY.getCode(), CATEGORY_EMPTY.getErrorMessage());
        }
        //카테고리 범위 오류
        if(postDto.getCategory() < 0 || postDto.getCategory() > 5){
            throw new CommunityException(CATEGORY_RANGE_ERR, CATEGORY_RANGE_ERR.getCode(), CATEGORY_RANGE_ERR.getErrorMessage());
        }
        //사이즈 null
        if(postDto.getSize() == null){
            throw new CommunityException(SIZE_EMPTY, SIZE_EMPTY.getCode(), SIZE_EMPTY.getErrorMessage());
        }
        //사이즈 범위 오류
        if(postDto.getSize() < 0 || postDto.getSize() > 4){
            throw new CommunityException(SIZE_RANGE_ERR, SIZE_RANGE_ERR.getCode(), SIZE_RANGE_ERR.getErrorMessage());
        }
        //배송 방법 null
        if(postDto.getDeliveryType() == null){
            throw new CommunityException(DELIVERY_TYPE_EMPTY, DELIVERY_TYPE_EMPTY.getCode(), DELIVERY_TYPE_EMPTY.getErrorMessage());
        }
        //배송 방법 범위 오류
        if(postDto.getDeliveryType()  < 0 || postDto.getDeliveryType() > 1){
            throw new CommunityException(DELIVERY_TYPE_RANGE_ERR, DELIVERY_TYPE_RANGE_ERR.getCode(), DELIVERY_TYPE_RANGE_ERR.getErrorMessage());
        }
        //상세설명 null
        if(postDto.getDetail() == null){
            throw new CommunityException(DETAIL_EMPTY, DETAIL_EMPTY.getCode(), DETAIL_EMPTY.getErrorMessage());
        }
        //판매글인데 가격이 null
        if(postDto.getPostType()==1 && postDto.getPrice() == null){
            throw new CommunityException(PRICE_EMPTY, PRICE_EMPTY.getCode(), PRICE_EMPTY.getErrorMessage());
        }
        //거래 방식이 택배 배송인데 배송비가 null
        if(postDto.getDeliveryType()==1 && postDto.getDeliveryFee()==null){
            throw new CommunityException(DELIVERY_FEE_EMPTY, DELIVERY_FEE_EMPTY.getCode(), DELIVERY_FEE_EMPTY.getErrorMessage());
        }
        //거래 방식이 직거래인데 거래 희망 지역이 null
        if (postDto.getDeliveryType() == 0 && postDto.getAddress() == null){
            throw new CommunityException(REGION_EMPTY, REGION_EMPTY.getCode(), REGION_EMPTY.getErrorMessage());
        }
        //이미지 null
        if(CollectionUtils.isEmpty(multipartFiles)){
            throw new CommunityException(IMAGE_EMPTY, IMAGE_EMPTY.getCode(), IMAGE_EMPTY.getErrorMessage());
        }
        //이미지 개수 5개 이하로 제한
        if(multipartFiles.size()>5){
            throw new CommunityException(IMAGE_LIMIT_EXCEEDED, IMAGE_LIMIT_EXCEEDED.getCode(), IMAGE_LIMIT_EXCEEDED.getErrorMessage());
        }
    }

    public void checkPostExceptionWithoutImage(PostDto postDto) {
        //제목 null
        if(postDto.getTitle() == null){
            throw new CommunityException(TITLE_EMPTY, TITLE_EMPTY.getCode(), TITLE_EMPTY.getErrorMessage());
        }
        //추천 성별 null
        if(postDto.getGender() == null){
            throw new CommunityException(GENDER_EMPTY, GENDER_EMPTY.getCode(), GENDER_EMPTY.getErrorMessage());
        }
        //추천 성별 범위 오류
        if(postDto.getGender() < 0 || postDto.getGender() > 1){
            throw new CommunityException(GENDER_RANGE_ERR, GENDER_RANGE_ERR.getCode(), GENDER_RANGE_ERR.getErrorMessage());
        }
        //게시글 타입 null
        if(postDto.getPostType() == null){
            throw new CommunityException(POST_TYPE_EMPTY, POST_TYPE_EMPTY.getCode(), POST_TYPE_EMPTY.getErrorMessage());
        }
        //게시글 타입 범위 오류
        if(postDto.getPostType()  < 0 || postDto.getPostType() > 1){
            throw new CommunityException(POST_TYPE_RANGE_ERR, POST_TYPE_RANGE_ERR.getCode(), POST_TYPE_RANGE_ERR.getErrorMessage());
        }
        //카테고리 null
        if(postDto.getCategory() == null){
            throw new CommunityException(CATEGORY_EMPTY, CATEGORY_EMPTY.getCode(), CATEGORY_EMPTY.getErrorMessage());
        }
        //카테고리 범위 오류
        if(postDto.getCategory() < 0 || postDto.getCategory() > 5){
            throw new CommunityException(CATEGORY_RANGE_ERR, CATEGORY_RANGE_ERR.getCode(), CATEGORY_RANGE_ERR.getErrorMessage());
        }
        //사이즈 null
        if(postDto.getSize() == null){
            throw new CommunityException(SIZE_EMPTY, SIZE_EMPTY.getCode(), SIZE_EMPTY.getErrorMessage());
        }
        //사이즈 범위 오류
        if(postDto.getSize() < 0 || postDto.getSize() > 4){
            throw new CommunityException(SIZE_RANGE_ERR, SIZE_RANGE_ERR.getCode(), SIZE_RANGE_ERR.getErrorMessage());
        }
        //배송 방법 null
        if(postDto.getDeliveryType() == null){
            throw new CommunityException(DELIVERY_TYPE_EMPTY, DELIVERY_TYPE_EMPTY.getCode(), DELIVERY_TYPE_EMPTY.getErrorMessage());
        }
        //배송 방법 범위 오류
        if(postDto.getDeliveryType()  < 0 || postDto.getDeliveryType() > 1){
            throw new CommunityException(DELIVERY_TYPE_RANGE_ERR, DELIVERY_TYPE_RANGE_ERR.getCode(), DELIVERY_TYPE_RANGE_ERR.getErrorMessage());
        }
        //상세설명 null
        if(postDto.getDetail() == null){
            throw new CommunityException(DETAIL_EMPTY, DETAIL_EMPTY.getCode(), DETAIL_EMPTY.getErrorMessage());
        }
        //판매글인데 가격이 null
        if(postDto.getPostType()==1 && postDto.getPrice() == null){
            throw new CommunityException(PRICE_EMPTY, PRICE_EMPTY.getCode(), PRICE_EMPTY.getErrorMessage());
        }
        //거래 방식이 택배 배송인데 배송비가 null
        if(postDto.getDeliveryType()==1 && postDto.getDeliveryFee()==null){
            throw new CommunityException(DELIVERY_FEE_EMPTY, DELIVERY_FEE_EMPTY.getCode(), DELIVERY_FEE_EMPTY.getErrorMessage());
        }
        //거래 방식이 직거래인데 거래 희망 지역이 null
        if (postDto.getDeliveryType() == 0 && postDto.getAddress() == null){
            throw new CommunityException(REGION_EMPTY, REGION_EMPTY.getCode(), REGION_EMPTY.getErrorMessage());
        }
    }

}
