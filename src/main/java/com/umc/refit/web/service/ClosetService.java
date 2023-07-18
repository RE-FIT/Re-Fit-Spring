package com.umc.refit.web.service;

import com.umc.refit.domain.dto.clothe.RegisterClotheRequestDto;
import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.web.repository.ClotheRepository;
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


@Slf4j
@RequiredArgsConstructor
@Service
public class ClosetService {

    private final ClotheRepository clotheRepository;

    private final MemberRepository memberRepository;

    private final S3UploadService s3UploadService;
    private final String bucketDirName = "closet";
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Transactional
    public Long registerClothe(RegisterClotheRequestDto request,
                               MultipartFile multipartFile,
                               Authentication authentication) {

        Member member = this.memberRepository.findByLoginId(
                        authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("No member found with this user id"));

        ImageDto imageDto;
        try {
            imageDto = this.s3UploadService.uploadFile(multipartFile, bucketName, bucketDirName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.clotheRepository.save(request.toEntity(member, imageDto))
                .getId();
    }
}
