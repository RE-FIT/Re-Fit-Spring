package com.umc.refit.web.service;

import com.umc.refit.domain.dto.mypage.GetMyInfoResponseDto;
import com.umc.refit.domain.dto.mypage.UpdateMyInfoRequestDto;
import com.umc.refit.domain.dto.mypage.UpdatePasswordRequestDto;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.exception.member.MemberException;
import com.umc.refit.exception.myInfo.MyInfoException;
import com.umc.refit.exception.validator.MemberValidator;
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

import static com.umc.refit.exception.ExceptionType.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyInfoService {


    private final S3UploadService s3UploadService;

    private final MemberService memberService;
    private final String bucketDirName = "myInfo";

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional(readOnly = true)
    public GetMyInfoResponseDto getMyInfo(Authentication authentication) {
        return GetMyInfoResponseDto.from(getMember(authentication));
    }

    @Transactional(readOnly = true)
    public Boolean checkInsertedName(String name) {
        return this.memberService.findMemberByName(name).isPresent();
    }


    @Transactional
    public void updateMyInfo(MultipartFile multipartFile, UpdateMyInfoRequestDto request, Authentication authentication) {

        if (null == multipartFile) {
            getMember(authentication).updateMemberByMyInfo(request, null);
            return;
        }

        ImageDto imageDto;
        try {
            imageDto = this.s3UploadService.uploadFile(Objects.requireNonNull(multipartFile), bucketName, bucketDirName);
            getMember(authentication).updateMemberByMyInfo(request, imageDto.getImageUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequestDto request, Authentication authentication) {

        // 1. Authentication 을 통해 Member 을 찾고
        // 2. 요청 CurrentPassword 와 Member 의 EncryptPassword 을 비교 by matches
        // 3. 비밀번호 수정 로직은 MemberService 위임
        Member member = this.getMember(authentication);

        if (!this.memberService.isPasswordMatch(request.getCurrentPassword(), member.getPassword())) {
            throw new MyInfoException(PASSWORD_IS_NOT_MATCH, PASSWORD_IS_NOT_MATCH.getCode(), PASSWORD_IS_NOT_MATCH.getErrorMessage());
        }
        passwordCheck(request.getNewPassword());
        this.memberService.updateMemberPassword(member, request.getNewPassword());
    }


    /*비밀번호 체크 메서드*/
    private void passwordCheck(String password) {
        //예외 코드 10014: 비밀번호는 필수 정보입니다.
        if (password.strip().equals("")) {
            throw new MemberException(PASSWORD_EMPTY, PASSWORD_EMPTY.getCode(), PASSWORD_EMPTY.getErrorMessage());
        }

        //예외 코드 10015: "8-16자의 영문 대소문자, 숫자, 특수문자 ((!), (_) , (-))를 포합해야합니다.
        if (!MemberValidator.isPasswordValid(password)) {
            throw new MemberException(PASSWORD_INVALID, PASSWORD_INVALID.getCode(), PASSWORD_INVALID.getErrorMessage());
        }
    }

    private Member getMember(Authentication authentication) {
        return this.memberService.findByLoginId(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"));
    }
}
