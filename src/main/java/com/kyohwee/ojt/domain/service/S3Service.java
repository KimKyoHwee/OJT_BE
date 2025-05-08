package com.kyohwee.ojt.domain.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 s3Client;

    public String uploadImage(MultipartFile file, String dirName) {
        String fileName = createFileName(file.getOriginalFilename(), dirName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.setContentLength(file.getSize());

        try {
            s3Client.putObject(new PutObjectRequest(
                    "ojt-ocrbuckets",
                    fileName,
                    file.getInputStream(),
                    objectMetadata
            ));
            return s3Client.getUrl("ojt-ocrbuckets", fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    private String createFileName(String originalFilename, String dirName) {
        if (originalFilename == null || dirName == null) {
            throw new IllegalArgumentException("파일명과 디렉토리명은 null일 수 없습니다.");
        }

        String sanitizedFilename = sanitizeFilename(originalFilename);
        return dirName + "/" + UUID.randomUUID() + "_" + sanitizedFilename;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

}
