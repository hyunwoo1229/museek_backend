// src/main/java/com/example/study3/service/GcsFileService.java

package com.example.study3.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
public class GcsFileService {

    private final Storage storage;
    private final String bucketName;

    public GcsFileService(@Value("${gcp.gcs.bucket-name}") String bucketName) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    public String uploadFromUrl(String sourceUrl, String destinationPath) throws Exception {
        URL fileUrl = new URL(sourceUrl);

        // 1. Suno URL에서 파일을 스트림으로 읽어옵니다.
        try (InputStream inputStream = fileUrl.openStream()) {

            // 2. GCS에 저장할 Blob 정보 생성
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, destinationPath)
                    .setContentType("audio/mpeg") // 파일 타입 지정
                    .setAcl(List.of(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER))) // 모든 사용자에게 읽기 권한 부여 (공개 파일)
                    .build();

            // 3. GCS에 파일 업로드
            storage.createFrom(blobInfo, inputStream);

            // 4. 파일의 공개 접근 URL 반환
            // GCS 기본 공개 URL 형식: https://storage.googleapis.com/[BUCKET_NAME]/[OBJECT_PATH]
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, destinationPath);
        }
    }
}