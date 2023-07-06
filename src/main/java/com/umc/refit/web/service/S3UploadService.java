package com.umc.refit.web.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.umc.refit.domain.dto.s3.ImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class S3UploadService {

    private final AmazonS3Client amazonS3Client;
    private final AmazonS3 s3;


    /*멀티파트파일을 지정한 s3 버킷의 폴더에 저장
    * ImageDto 객체에 key name, url 넣어서 반환
    * */
    public ImageDto uploadFile(MultipartFile multipartFile, String bucketName, String bucketDirName) throws IOException {

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        String keyName = bucketDirName + "/" + storeFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());

        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, multipartFile.getInputStream(), metadata).withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(request);

        String uploadImageUrl = amazonS3Client.getUrl(bucketName, keyName).toString();
        ImageDto imageDto = new ImageDto(keyName, uploadImageUrl);

        return imageDto;
    }


    /*이름 겹치지 않게 UUID 사용*/
    private String createStoreFileName(String originalFileName) {
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /*업로드 파일에서 확장자 추출*/
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}