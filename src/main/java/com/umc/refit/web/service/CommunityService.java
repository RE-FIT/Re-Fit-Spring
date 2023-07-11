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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final MemberService memberService;
    private final CmImgService cmImgService;


    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    private String bucketDirName = "community";


    /*이미지 포함한 게시글 저장*/
    public void create(PostDto postDto, List<MultipartFile> multipartFiles,Authentication authentication) throws IOException {

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
    }

    public Posts save(Posts post) {
        return communityRepository.save(post);
    }


    /*들어온 값에 따라 postDto 값 설정*/
    private PostDto setPostDto(Authentication authentication, PostDto postDto) {
        String userId = authentication.getName();

        Optional<Member> findMember = memberService.findMemberByLoginId(userId);

        if(findMember.isEmpty()) {
            throw new IllegalStateException("회원 계정이 존재하지 않습니다.");
        }

        postDto.setMember(findMember.get());

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


}
