package com.sandeep.postservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

     private final S3Client s3Client;

     @Value("${aws.s3.bucket-name}")
     private String bucketName;

    @Value("${aws.region}")
     private String region;

    public String uploadFile(MultipartFile file, String keyPrefix) {

        try{

              String key= keyPrefix+ "/"+ UUID.randomUUID()+"_"+file.getOriginalFilename();

            PutObjectRequest request=PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

            log.info("File uploaded to s3: {}",url);

            return url;

        } catch (Exception e) {

             throw new RuntimeException("Failed to upload file to s3: "+e.getMessage());


        }

    }
}
