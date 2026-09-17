package com.vivek.HospitalManagement.Service.Reports;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file) {

        String storageKey =
                "imaging/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return storageKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public InputStream getFile(String storageKey){

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build());

        } catch (Exception e) {
            throw new RuntimeException("Failed to Retrieve the File" + e);
        }

    }
}