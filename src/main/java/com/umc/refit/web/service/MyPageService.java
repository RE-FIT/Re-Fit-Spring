package com.umc.refit.web.service;

import com.umc.refit.domain.dto.mypage.GetMyInfoResponseDto;
import com.umc.refit.domain.dto.mypage.UpdateMyInfoRequestDto;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.web.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyPageService {

    private final MemberRepository memberRepository;

    private final S3UploadService s3UploadService;

    private final String bucketDirName = "myInfo";

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional(readOnly = true)
    public GetMyInfoResponseDto getMyInfo(Authentication authentication) {
        return GetMyInfoResponseDto.from(this.memberRepository.findByLoginId(
                        authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id")));
    }

    @Transactional(readOnly = true)
    public Boolean checkInsertedName(String name) {
        return this.memberRepository.findByName(name).isPresent();
    }


    @Transactional
    public void updateMyInfo(MultipartFile multipartFile, UpdateMyInfoRequestDto request, Authentication authentication) {

        if (null == multipartFile) {
            this.memberRepository.findByLoginId(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"))
                    .updateMemberByMyInfo(request, null);
            return;
        }

        ImageDto imageDto;
        try {
            imageDto = this.s3UploadService.uploadFile(Objects.requireNonNull(multipartFile), bucketName, bucketDirName);
            this.memberRepository.findByLoginId(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"))
                    .updateMemberByMyInfo(request, imageDto.getImageUrl());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
