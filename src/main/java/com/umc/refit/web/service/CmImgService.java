package com.umc.refit.web.service;

import com.umc.refit.domain.dto.s3.ImageDto;
import com.umc.refit.domain.entity.PostImage;
import com.umc.refit.domain.entity.Posts;
import com.umc.refit.web.repository.CmImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CmImgService {

    private  final CmImgRepository cmImgRepository;
    private final S3UploadService s3UploadService;


    /*이미지 파일을 S3에 저장*/
    public List<PostImage> uploadPostImg(List<MultipartFile> multipartFiles, String bucketName, String bucketDirName){
        List<PostImage> imageList = new ArrayList<>();

        /*
        * S3에 이미지 저장 후 ImageDto 반환 받고
        * ImageDto 이용해서 PostImage 객체 생성
        * PostImage 객체 리스트 반환
        * */
        multipartFiles.forEach(multipartFile -> {
            ImageDto imageDto = s3UploadService.uploadFile(multipartFile, bucketName, bucketDirName);

            PostImage image = new PostImage(imageDto);
            imageList.add(image);
        });
        return imageList;
    }


    /*이미지 객체와 게시글 객체 양방향 연결 후
    * 이미지 객체 저장
    * */
    public void savePostImg(PostImage image, Posts post){
        image = cmImgRepository.save(image);
        image.setPost(post);
    }


    public void deletePostImg(String bucketName, String keyName){
        s3UploadService.deleteFile(bucketName, keyName);
    }


}
