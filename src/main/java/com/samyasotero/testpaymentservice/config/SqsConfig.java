package com.samyasotero.testpaymentservice.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    /*@Value("${custom.aws.access-key}")
    private String accessKey;

    @Value("${custom.aws.secret-key}")
    private String secretKey;

    @Value("${custom.aws.region}")
    private String region;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {

        AwsBasicCredentials credenciais = AwsBasicCredentials.create(accessKey, secretKey);

        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credenciais))
                .build();
    }
*/
    @Bean
    public SqsMessagingMessageConverter sqsMessagingMessageConverter() {
        JacksonJsonMessageConverter jacksonConverter = new JacksonJsonMessageConverter();
        jacksonConverter.setSerializedPayloadClass(String.class);

        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        converter.setPayloadMessageConverter(jacksonConverter);

        return converter;
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient,
            SqsMessagingMessageConverter sqsMessagingMessageConverter) {

        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options
                        .queueNotFoundStrategy(QueueNotFoundStrategy.FAIL)
                        .messageConverter(sqsMessagingMessageConverter))
                .build();
    }
}