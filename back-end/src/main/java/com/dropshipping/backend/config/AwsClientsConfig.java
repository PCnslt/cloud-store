package com.dropshipping.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class AwsClientsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    private Region getRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public TextractClient textractClient() {
        return TextractClient.builder()
                .region(getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
