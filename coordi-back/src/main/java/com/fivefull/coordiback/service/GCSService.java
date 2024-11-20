package com.fivefull.coordiback.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class GCSService {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Value("${gcs.key.file.path}")
    private String keyFilePath;

    private Storage storage;

    @PostConstruct
    private void initializeGCS() throws IOException {
        storage = StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(keyFilePath)))
                .build()
                .getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = generateFileName(file.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
        storage.create(blobInfo, file.getBytes());
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }

    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }

    public String uploadBase64Image(String base64Image) {
        try {
            String[] parts = base64Image.split(",");
            byte[] imageBytes = java.util.Base64.getDecoder().decode(parts[1]); // Base64 데이터 디코딩
            String fileName = generateFileName("base64_image.png");
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
            storage.create(blobInfo, imageBytes);
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
        } catch (Exception e) {
            throw new RuntimeException("Base64 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
