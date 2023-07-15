package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.*;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.PostImage;
import com.umc.refit.domain.entity.Posts;
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
                post.getRegion(),
                post.getPrice(),
                post.getDetail(),
                post.getPostType());
        return clickedPost;
    }



    /*커뮤니티 메인 화면 - 나눔*/
    public List<PostMainResponseDto> communityShareMain(Integer postType, Integer gender, Integer category, Authentication authentication){
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        //유저가 차단한 멤버
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        List<Posts> posts = findSharePost(postType, gender, category, blockMemIds);
        List<PostMainResponseDto> sharePosts = convertToDtoList(posts);

        return sharePosts;
    }

    /*커뮤니티 메인 화면 - 판매*/
    public List<PostMainResponseDto> communitySellMain(Integer postType, Integer gender, Integer category, String region, Authentication authentication){

        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        //유저가 차단한 멤버 아이디 목록
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        List<Posts> posts = findSellPost(postType, gender, category, region, blockMemIds);
        List<PostMainResponseDto> sellPosts = convertToDtoList(posts);

        return sellPosts;
    }


    /*선택한 카테고리에 맞는 나눔 글 리스트*/
    public List<Posts> findSharePost(Integer postType, Integer gender, Integer category, List<Long> blockMemIds){
        List<Posts> posts = communityRepository.findByPostTypeAndGenderAndCategoryAndPostState(postType, gender, category, 0);
        posts = filterBlockedPosts(posts, blockMemIds);
        return posts;
    }

    /*선택한 카테고리에 맞는 판매 글 리스트*/
    public List<Posts> findSellPost(Integer postType, Integer gender, Integer category, String region, List<Long> blockMemIds){
        List<Posts> posts = communityRepository.findByPostTypeAndGenderAndCategoryAndRegionAndPostState(postType, gender, category, region,1);
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
                        posts.getRegion(),
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
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

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
        } else if (postDto.getDeliveryType() == 1) {
            //택배면 거래 지역을 전국으로 설정
            postDto.setRegion("전국");
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
        } else if (postDto.getDeliveryType() == 1) {
            //택배면 거래 지역을 전국으로 설정
            postDto.setRegion("전국");
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
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        //유저가 차단한 멤버 아이디 목록
        List<Long> blockMemIds = blockService.getBlockMemIds(member);

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found with this post id"));

        if(blockMemIds.contains(findPost.getMember().getId())){
            throw new IllegalStateException("차단한 유저의 글입니다.");
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
                findPost.getRegion(),
                findPost.getPrice(),
                findPost.getDetail(),
                findPost.getPostType());
        return clickedPost;
    }

    /*게시글 삭제*/
    public void deletePost(Long postId, Authentication authentication) {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found with this post id"));

        if(member.getId().equals(findPost.getMember().getId())){
            //이미지 s3에서 삭제
            List<PostImage> images = findPost.getImage();
            for (PostImage image : images) {
                cmImgService.deletePostImg(bucketName, image.getS3key());
            }

            communityRepository.delete(findPost);
        }else{
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
    }

    /*게시글 상태 변경*/
    public void changeState(Long postId, Authentication authentication) {
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found with this post id"));

        if(!member.getId().equals(findPost.getMember().getId())){
            throw new IllegalStateException("이 게시글 상태 변경 권한이 없습니다.");
        }

        //나눔 완료 -> 나눔 중
        if(findPost.getPostState().equals(2)){
            findPost.changeState(0);
            findPost.removeBuyer();
            return;
        }

        //판매 완료 -> 판매 중
        if(findPost.getPostState().equals(3)){
            findPost.changeState(1);
            findPost.removeBuyer();
            return;
        }
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
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        Posts findPost = findPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found with this post id"));

        if(!member.getId().equals(findPost.getMember().getId())){
            throw new IllegalStateException("이 게시글 수정 권한이 없습니다.");
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
                findPost.getRegion(),
                findPost.getPrice(),
                findPost.getDetail(),
                findPost.getPostType());
        return clickedPost;
    }


    /*마이페이지 내 피드*/
    public List<PostMainResponseDto> myFeedPosts(Integer postType, Authentication authentication){
        //로그인 유저
        String userId = authentication.getName();
        Member member = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        List<Posts> posts = communityRepository.findByMemberAndPostType(member, postType);

        List<PostMainResponseDto> postList = convertToDtoList(posts);
        return postList;
    }

}
