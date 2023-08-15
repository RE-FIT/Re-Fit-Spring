package com.umc.refit.web.service;

import com.umc.refit.domain.dto.chat.TradeRequestDto;
import com.umc.refit.domain.dto.community.*;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.PostImage;
import com.umc.refit.domain.entity.Posts;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.web.repository.CommunityRepository;
import com.umc.refit.web.repository.CommunityRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.umc.refit.exception.ExceptionType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityRepositoryImpl communityRepositoryImpl;
    private final BlockService blockService;
    private final MemberService memberService;
    private final CmImgService cmImgService;


    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private final String bucketDirName = "community";


    /*이미지 포함한 게시글 저장*/
    public PostClickResponseDto create(PostDto postDto, List<MultipartFile> multipartFiles,Authentication authentication){

        PostDto newPostDto = setPostDto(authentication, postDto);
        //게시글 저장
        Posts post = communityRepository.save(new Posts(newPostDto));

        //이미지 파일을 S3에 저장
        List<PostImage> imageList = cmImgService.uploadPostImg(multipartFiles, bucketName, bucketDirName);

        //이미지 파일을 DB에 저장
        if(!imageList.isEmpty()) {
            for(PostImage image : imageList) {
                cmImgService.savePostImg(image, post);
            }
        }

        List<String> imgUrls = post.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        return new PostClickResponseDto(
                post.getMember().getName(),
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getCategory(),
                post.getGender(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getAddress(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState(),
                post.getCreatedAt(),
                false);
    }


    /*커뮤니티 메인 화면*/
    public List<PostMainResponseDto> communityMainPosts(Integer postType, Integer gender, Integer category, Authentication authentication, ScrapService scrapService){

        Member member = findMember(authentication);

        //유저가 차단한 멤버
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        List<Posts> postsList = communityRepositoryImpl.communityMainPage(postType, gender, category);
        postsList = filterBlockedPosts(postsList, blockMemIds);

        return convertToDtoListMain(postsList, scrapService, member);
    }



    /*(새로운 글 작성 시) 들어온 값에 따라 postDto 값 설정*/
    private PostDto setPostDto(Authentication authentication, PostDto postDto) {
        Member member = findMember(authentication);

        postDto.setMember(member);

        //나눔 글이면 상품 가격 0원, 글 상태를 나눔 중으로 설정
        if (postDto.getPostType().equals(0)){
            postDto.setPrice(0);
            postDto.setPostState(0);
        } else if (postDto.getPostType().equals(1)) {
            //판매 글이면 글 상태를 판매 중으로 설정
            postDto.setPostState(1);
        }

        //직거래면 배송비 0원으로 설정
        if (postDto.getDeliveryType().equals(0)){
            postDto.setDeliveryFee(0);
        }
        return postDto;
    }


    /*게시글 상세 조회*/
    public PostClickResponseDto clickPost(Long postId, Authentication authentication, ScrapService scrapService) {

        Member member = findMember(authentication);
        //유저가 차단한 멤버 아이디 목록
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        Posts post = findPost(postId);

        if(blockMemIds.contains(post.getMember().getId())){
            throw new CommunityException(BLOCKED_USER_POST, BLOCKED_USER_POST.getCode(), BLOCKED_USER_POST.getErrorMessage());
        }

        //유저가 해당 글을 스크랩 했는지 확인
        boolean scrapFlag = scrapService.isPostScrappedByUser(member, postId);


        List<String> imgUrls = post.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        return new PostClickResponseDto(
                member.getName(),
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getCategory(),
                post.getGender(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getAddress(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState(),
                post.getCreatedAt(),
                scrapFlag);
    }

    /*게시글 삭제*/
    public void deletePost(Long postId, Authentication authentication) {

        Member member = findMember(authentication);

        Posts post = findPost(postId);

        checkPostPermisstion(member, post);

        //이미지 s3에서 삭제
        List<PostImage> images = post.getImage();
        for (PostImage image : images) {
            cmImgService.deletePostImg(bucketName, image.getS3key());
        }

        communityRepository.delete(post);

    }

    /*게시글 상태 변경*/
    public PostClickResponseDto changeState(Long postId, Authentication authentication) {

        Member member = findMember(authentication);

        Posts post = findPost(postId);

        checkPostPermisstion(member, post);

        //나눔 완료 -> 나눔 중
        if(post.getPostState().equals(2)){
            post.changeState(0);
            post.removeBuyer();
        }

        //판매 완료 -> 판매 중
        if(post.getPostState().equals(3)){
            post.changeState(1);
            post.removeBuyer();
        }

        List<String> imgUrls = post.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        return new PostClickResponseDto(
                member.getName(),
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getCategory(),
                post.getGender(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getAddress(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState(),
                post.getCreatedAt(),
                false);
    }

    /*게시글 검색*/
    public List<PostMainResponseDto> searchPosts(Authentication authentication, String keyword, ScrapService scrapService, Integer postType, Integer gender, Integer category) {

        Member member = findMember(authentication);

        List<Posts> postsList = communityRepositoryImpl.searchPosts(postType, gender, category, keyword);

        return convertToDtoListMain(postsList, scrapService, member);
    }


    /*게시글 수정 - 이미지 수정 o*/
    public PostClickResponseDto update(Long postId, PostDto postDto, List<MultipartFile> multipartFiles, Authentication authentication) {

        Member member = findMember(authentication);

        Posts post = findPost(postId);

        checkPostPermisstion(member, post);

        //기존 이미지 s3에서 삭제
        List<PostImage> images = post.getImage();
        for (PostImage image : images) {
            cmImgService.deletePostImg(bucketName, image.getS3key());
        }

        //기존 이미지 DB에서 삭제
        post.getImage().clear();

        //새로운 이미지 파일을 S3에 저장
        List<PostImage> imageList = cmImgService.uploadPostImg(multipartFiles, bucketName, bucketDirName);

        //이미지 파일을 DB에 저장
        if(!imageList.isEmpty()) {
            for(PostImage image : imageList) {
                cmImgService.savePostImg(image, post);
            }
        }

        PostDto newPostDto = setChangePostDto(postDto, post);
        post.update(newPostDto);

        List<String> imgUrls = post.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        return new PostClickResponseDto(
                member.getName(),
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getCategory(),
                post.getGender(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getAddress(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState(),
                post.getCreatedAt(),
                false);
    }


    /*게시글 수정 - 이미지 수정 x*/
    public PostClickResponseDto updateWithoutImage(Long postId, PostDto postDto, Authentication authentication) {

        Member member = findMember(authentication);

        Posts post = findPost(postId);

        checkPostPermisstion(member, post);

        PostDto newPostDto = setChangePostDto(postDto, post);
        post.update(newPostDto);

        List<String> imgUrls = post.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        return new PostClickResponseDto(
                member.getName(),
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getCategory(),
                post.getGender(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getAddress(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState(),
                post.getCreatedAt(),
                false);
    }



    /*마이페이지 내 피드 - 구매*/
    public List<PostMyPageResponseDto> myFeedBuy(Authentication authentication){

        Member member = findMember(authentication);

        List<Posts> posts = communityRepository.findByBuyerOrderByCreatedAtDesc(member);

        return convertToDtoListMyFeed(posts);
    }

    /*마이페이지 내 피드 - 내 판매,나눔 글*/
    public List<PostMyPageResponseDto> myFeedPosts(Integer postType, Authentication authentication){

        Member member = findMember(authentication);

        List<Posts> posts = communityRepository.findByMemberAndPostTypeOrderByCreatedAtDesc(member, postType);

        return convertToDtoListMyFeed(posts);
    }

    /*거래 완료*/
    public void trade(TradeRequestDto tradeRequestDto, Authentication authentication) {

        Member member = findMember(authentication);
        Long postId = tradeRequestDto.getPostId();

        Posts post = findPost(postId);
        checkPostPermisstion(member, post);

        //구매자
        Member buyer = findBuyingMember(tradeRequestDto);

        if(post.getBuyer() == null) {
            if(post.getPostState().equals(0)){
                post.changeState(2);
                post.initializeBuyer(buyer);
            }
            if(post.getPostState().equals(1)){
                post.changeState(3);
                post.initializeBuyer(buyer);
            }
        }else{
            throw new CommunityException(ALREADY_COMPLETED_TRADE, ALREADY_COMPLETED_TRADE.getCode(), ALREADY_COMPLETED_TRADE.getErrorMessage());
        }
    }


    /*차단한 유저의 글 제외*/
    public List<Posts> filterBlockedPosts(List<Posts> posts, List<Long> blockedMemberIds) {
        return posts.stream()
                .filter(post -> !blockedMemberIds.contains(post.getMember().getId()))
                .collect(Collectors.toList());
    }


    /*(글 수정 시) 들어온 값에 따라 postDto 값 설정*/
    private PostDto setChangePostDto(PostDto postDto, Posts findPost) {
        //나눔 완료된 글 수정할 때 거래방식이 '나눔'일 경우 나눔 완료로 유지
        if(findPost.getPostState().equals(2) && postDto.getPostType().equals(0)){
            postDto.setPostState(2);
            postDto.setPrice(0);
        }
        //나눔 완료된 글  수정할 때 거래방식이 ‘판매’일 경우 판매 중으로 상태 변경
        if(findPost.getPostState().equals(2) && postDto.getPostType().equals(1)){
            postDto.setPostState(1);
            findPost.removeBuyer();
        }
        //판매 완료된 글 수정할 때 거래방식이 '판매'일 경우 판매 완료로 유지
        if(findPost.getPostState().equals(3) && postDto.getPostType().equals(1)){
            postDto.setPostState(3);
        }
        //판매 완료된 글 수정할 때 거래방식이 ‘나눔’일 경우 나눔 중으로 상태 변경
        if(findPost.getPostState().equals(2) && postDto.getPostType().equals(1)){
            postDto.setPostState(0);
            postDto.setPrice(0);
            findPost.removeBuyer();
        }
        //나눔 중 글 수정할 때 거래방식이 '나눔'일 경우 나눔 중으로 유지
        if(findPost.getPostState().equals(0) && postDto.getPostType().equals(0)){
            postDto.setPostState(0);
            postDto.setPrice(0);
        }

        //나눔 중 글 수정할 때 거래방식이 '판매'일 경우 판매 중으로 변경
        if(findPost.getPostState().equals(0) && postDto.getPostType().equals(1)){
            postDto.setPostState(1);
        }

        //판매 중 글 수정할 때 거래방식이 '판매'일 경우 판매 중으로 유지
        if(findPost.getPostState().equals(1) && postDto.getPostType().equals(1)){
            postDto.setPostState(1);
        }

        //판매 중 글 수정할 때 거래방식이 '나눔'일 경우 나눔 중으로 변경
        if(findPost.getPostState().equals(1) && postDto.getPostType().equals(0)){
            postDto.setPostState(0);
            postDto.setPrice(0);
        }

        //직거래면 배송비 0원으로 설정
        if (postDto.getDeliveryType().equals(0)){
            postDto.setDeliveryFee(0);
        }
        return postDto;
    }

    /*Posts 객체를 PostMainResponseDto 객체로 변환*/
    public List<PostMainResponseDto> convertToDtoListMain(List<Posts> postsList, ScrapService scrapService, Member member) {
        return postsList.stream()
                .map(posts -> {
                    boolean scrapFlag = scrapService.isPostScrappedByUser(member, posts.getId());
                    return new PostMainResponseDto(
                            posts.getId(),
                            posts.getTitle(),
                            posts.getImage().get(0).getImageUrl(),
                            posts.getGender(),
                            posts.getDeliveryType(),
                            posts.getAddress(),
                            posts.getPrice(),
                            posts.getSize(),
                            scrapFlag);
                })
                .collect(Collectors.toList());
    }

    /*Posts 객체를 PostMyPageResponseDto 객체로 변환*/
    public List<PostMyPageResponseDto> convertToDtoListMyFeed(List<Posts> postsList) {
        return postsList.stream()
                .map(posts -> new PostMyPageResponseDto(
                        posts.getId(),
                        posts.getTitle(),
                        posts.getImage().get(0).getImageUrl(),
                        posts.getGender(),
                        posts.getDeliveryType(),
                        posts.getAddress(),
                        posts.getPrice(),
                        posts.getSize()
                ))
                .collect(Collectors.toList());
    }


    /*로그인 유저 확인, 반환*/
    public Member findMember(Authentication authentication){
        String userId = authentication.getName();
        return memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));
    }


    /*구매 유저 확인, 반환*/
    public Member findBuyingMember(TradeRequestDto tradeRequestDto){
        String buyerName = tradeRequestDto.getUsername();
        return memberService.findMemberByName(buyerName)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));
    }

    /*로그인 유저가 글 작성자인지 확인*/
    public void checkPostPermisstion(Member member, Posts post){
        if(!post.getMember().getId().equals(member.getId())){
            throw new CommunityException(PERMISSION_DENIED, PERMISSION_DENIED.getCode(), PERMISSION_DENIED.getErrorMessage());
        }
    }

    /*게시글 확인, 반환*/
    public Posts findPost(Long postId){
        return findPostById(postId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));
    }

    public Optional<Posts> findPostById(Long postId) {
        return communityRepository.findById(postId);
    }


}
