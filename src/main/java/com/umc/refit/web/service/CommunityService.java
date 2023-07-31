package com.umc.refit.web.service;

import com.umc.refit.domain.dto.chat.TradeRequestDto;
import com.umc.refit.domain.dto.community.*;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.PostImage;
import com.umc.refit.domain.entity.Posts;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.web.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.umc.refit.exception.ExceptionType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final BlockService blockService;
    private final MemberService memberService;
    private final CmImgService cmImgService;


    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private String bucketDirName = "community";


    /*이미지 포함한 게시글 저장*/
    public PostClickResponseDto create(PostDto postDto, List<MultipartFile> multipartFiles,Authentication authentication) throws IOException {

        PostDto newPostDto = setPostDto(authentication, postDto);
        //게시글 저장
        Posts post = save(new Posts(newPostDto));

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

        PostClickResponseDto clickedPost = new PostClickResponseDto(
                post.getId(),
                post.getTitle(),
                post.getMember().getName(),
                imgUrls,
                post.getSize(),
                post.getDeliveryType(),
                post.getDeliveryFee(),
                post.getSido(),
                post.getSigungu(),
                post.getBname(),
                post.getBname2(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType(),
                post.getPostState());
        return clickedPost;
    }



    /*커뮤니티 메인 화면*/
    public List<PostMainResponseDto> communityMainPosts(Integer postType, Integer gender, Integer category, Authentication authentication){
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() ->  new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        //유저가 차단한 멤버
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        List<Posts> posts = findPostList(postType, gender, category, blockMemIds);
        List<PostMainResponseDto> sharePosts = convertToDtoList(posts);

        return sharePosts;
    }


    /*선택한 카테고리에 맞는 글 리스트*/
    public List<Posts> findPostList(Integer postType, Integer gender, Integer category, List<Long> blockMemIds){
        List<Posts> posts = communityRepository.findByPostTypeAndGenderAndCategoryAndPostState(postType, gender, category, 0);
        posts = filterBlockedPosts(posts, blockMemIds);
        return posts;
    }

    /*차단한 유저의 글 제외*/
    public List<Posts> filterBlockedPosts(List<Posts> posts, List<Long> blockedMemberIds) {
        return posts.stream()
                .filter(post -> !blockedMemberIds.contains(post.getMember().getId()))
                .collect(Collectors.toList());
    }

    /*Posts 객체를 PostMainResponseDto 객체로 변환*/
    public List<PostMainResponseDto> convertToDtoList(List<Posts> postsList) {
        return postsList.stream()
                .map(posts -> new PostMainResponseDto(
                        posts.getId(),
                        posts.getTitle(),
                        posts.getImage().get(0).getImageUrl(),
                        posts.getGender(),
                        posts.getDeliveryType(),
                        posts.getSido(),
                        posts.getSigungu(),
                        posts.getBname(),
                        posts.getBname2(),
                        posts.getPrice()
                ))
                .collect(Collectors.toList());
    }

    public Posts save(Posts post) {
        return communityRepository.save(post);
    }


    /*(새로운 글 작성 시) 들어온 값에 따라 postDto 값 설정*/
    private PostDto setPostDto(Authentication authentication, PostDto postDto) {
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() ->  new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        postDto.setMember(member);

        //나눔 글이면 상품 가격 0원, 글 상태를 나눔 중으로 설정
        if (postDto.getPostType() == 0){
            postDto.setPrice(0);
            postDto.setPostState(0);
        } else if (postDto.getPostType() == 1) {
            //판매 글이면 글 상태를 판매 중으로 설정
            postDto.setPostState(1);
        }

        //직거래면 배송비 0원으로 설정
        if (postDto.getDeliveryType() == 0){
            postDto.setDeliveryFee(0);
        }
        return postDto;
    }


    /*(글 수정 시) 들어온 값에 따라 postDto 값 설정*/
    private PostDto setChangePostDto(PostDto postDto, Posts findPost) {
        //나눔 완료된 글 수정할 때 거래방식이 '나눔'일 경우 나눔 완료로 유지
        if(findPost.getPostState()==2 && postDto.getPostType() == 0){
            postDto.setPostState(2);
            postDto.setPrice(0);
        }
        //나눔 완료된 글  수정할 때 거래방식이 ‘판매’일 경우 판매 중으로 상태 변경
        if(findPost.getPostState()==2 && postDto.getPostType() == 1){
            postDto.setPostState(1);
            findPost.removeBuyer();
        }
        //판매 완료된 글 수정할 때 거래방식이 '판매'일 경우 판매 완료로 유지
        if(findPost.getPostState()==3 && postDto.getPostType() == 1){
            postDto.setPostState(3);
        }
        //판매 완료된 글 수정할 때 거래방식이 ‘나눔’일 경우 나눔 중으로 상태 변경
        if(findPost.getPostState()==2 && postDto.getPostType() == 1){
            postDto.setPostState(1);
            postDto.setPrice(0);
            findPost.removeBuyer();
        }
        //직거래면 배송비 0원으로 설정
        if (postDto.getDeliveryType() == 0){
            postDto.setDeliveryFee(0);
        }
        return postDto;
    }


    public Optional<Posts> findPostById(Long postId) {
        return communityRepository.findById(postId);
    }

    /*게시글 상세 조회*/
    public PostClickResponseDto clickPost(Long postId, Authentication authentication) {

        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() ->  new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        //유저가 차단한 멤버 아이디 목록
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        Posts findPost = findPostById(postId)
                .orElseThrow(() ->  new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));

        if(blockMemIds.contains(findPost.getMember().getId())){
            throw new CommunityException(BLOCKED_USER_POST, BLOCKED_USER_POST.getCode(), BLOCKED_USER_POST.getErrorMessage());
        }

        List<String> imgUrls = findPost.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        PostClickResponseDto clickedPost = new PostClickResponseDto(
                findPost.getId(),
                findPost.getTitle(),
                findPost.getMember().getName(),
                imgUrls,
                findPost.getSize(),
                findPost.getDeliveryType(),
                findPost.getDeliveryFee(),
                findPost.getSido(),
                findPost.getSigungu(),
                findPost.getBname(),
                findPost.getBname2(),
                findPost.getPrice(),
                findPost.getDetail(),
                findPost.getPostType(),
                findPost.getPostState());
        return clickedPost;
    }

    /*게시글 삭제*/
    public void deletePost(Long postId, Authentication authentication) {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));

        if(member.getId().equals(findPost.getMember().getId())){
            //이미지 s3에서 삭제
            List<PostImage> images = findPost.getImage();
            for (PostImage image : images) {
                cmImgService.deletePostImg(bucketName, image.getS3key());
            }

            communityRepository.delete(findPost);
        }else{
            throw new CommunityException(PERMISSION_DENIED, PERMISSION_DENIED.getCode(), PERMISSION_DENIED.getErrorMessage());
        }
    }

    /*게시글 상태 변경*/
    public PostClickResponseDto changeState(Long postId, Authentication authentication) {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));

        if(!member.getId().equals(findPost.getMember().getId())){
            throw new CommunityException(PERMISSION_DENIED, PERMISSION_DENIED.getCode(), PERMISSION_DENIED.getErrorMessage());
        }

        //나눔 완료 -> 나눔 중
        if(findPost.getPostState().equals(2)){
            findPost.changeState(0);
            findPost.removeBuyer();
        }

        //판매 완료 -> 판매 중
        if(findPost.getPostState().equals(3)){
            findPost.changeState(1);
            findPost.removeBuyer();
        }

        List<String> imgUrls = findPost.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        PostClickResponseDto changedPost = new PostClickResponseDto(
                findPost.getId(),
                findPost.getTitle(),
                findPost.getMember().getName(),
                imgUrls,
                findPost.getSize(),
                findPost.getDeliveryType(),
                findPost.getDeliveryFee(),
                findPost.getSido(),
                findPost.getSigungu(),
                findPost.getBname(),
                findPost.getBname2(),
                findPost.getPrice(),
                findPost.getDetail(),
                findPost.getPostType(),
                findPost.getPostState());
        return changedPost;
    }

    /*게시글 검색*/
    public List<PostMainResponseDto> searchPosts(String keyword) {
        return convertToDtoList(communityRepository.findByTitleContainingIgnoreCaseAndPostStateCustom(keyword));
    }


    /*게시글 수정*/
    public PostClickResponseDto update(Long postId, PostDto postDto, List<MultipartFile> multipartFiles, Authentication authentication) throws IOException {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));

        if(!member.getId().equals(findPost.getMember().getId())){
            throw new CommunityException(PERMISSION_DENIED, PERMISSION_DENIED.getCode(), PERMISSION_DENIED.getErrorMessage());
        }

        //기존 이미지 s3에서 삭제
        List<PostImage> images = findPost.getImage();
        for (PostImage image : images) {
            cmImgService.deletePostImg(bucketName, image.getS3key());
        }

        //기존 이미지 DB에서 삭제
        findPost.getImage().clear();

        //새로운 이미지 파일을 S3에 저장
        List<PostImage> imageList = cmImgService.uploadPostImg(multipartFiles, bucketName, bucketDirName);

        //이미지 파일을 DB에 저장
        if(!imageList.isEmpty()) {
            for(PostImage image : imageList) {
                cmImgService.savePostImg(image, findPost);
            }
        }

        PostDto newPostDto = setChangePostDto(postDto, findPost);
        findPost.update(newPostDto);

        List<String> imgUrls = findPost.getImage().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        PostClickResponseDto clickedPost = new PostClickResponseDto(
                findPost.getId(),
                findPost.getTitle(),
                findPost.getMember().getName(),
                imgUrls,
                findPost.getSize(),
                findPost.getDeliveryType(),
                findPost.getDeliveryFee(),
                findPost.getSido(),
                findPost.getSigungu(),
                findPost.getBname(),
                findPost.getBname2(),
                findPost.getPrice(),
                findPost.getDetail(),
                findPost.getPostType(),
                findPost.getPostState());
        return clickedPost;
    }


    /*마이페이지 내 피드 - 내 판매,나눔 글*/
    public List<PostMainResponseDto> myFeedPosts(Integer postType, Authentication authentication){
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        List<Posts> posts = communityRepository.findByMemberAndPostType(member, postType);

        List<PostMainResponseDto> postList = convertToDtoList(posts);
        return postList;
    }

    /*마이페이지 내 피드 - 구매*/
    public List<PostMainResponseDto> myFeedBuy(Authentication authentication){
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        List<Posts> posts = communityRepository.findByBuyer(member);

        List<PostMainResponseDto> postList = convertToDtoList(posts);
        return postList;
    }

    /*거래 완료*/
    public void trade(TradeRequestDto tradeRequestDto, Authentication authentication) {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        Posts findPost = findPostById(tradeRequestDto.getPostId())
                .orElseThrow(() -> new CommunityException(NO_SUCH_POST, NO_SUCH_POST.getCode(), NO_SUCH_POST.getErrorMessage()));

        if(!member.getId().equals(findPost.getMember().getId())){
            throw new CommunityException(PERMISSION_DENIED, PERMISSION_DENIED.getCode(), PERMISSION_DENIED.getErrorMessage());
        }

        //구매자
        String buyerName = tradeRequestDto.getUsername();
        Member buyer = memberService.findMemberByName(buyerName)
                .orElseThrow(() -> new CommunityException(NO_SUCH_MEMBER, NO_SUCH_MEMBER.getCode(), NO_SUCH_MEMBER.getErrorMessage()));

        if(findPost.getBuyer() == null) {
            if(findPost.getPostState().equals(0)){
                findPost.changeState(2);
                findPost.initializeBuyer(buyer);
            }
            if(findPost.getPostState().equals(1)){
                findPost.changeState(3);
                findPost.initializeBuyer(buyer);
            }
        }else{
            throw new IllegalStateException("이미 거래 완료된 상품입니다.");
        }
    }

}
