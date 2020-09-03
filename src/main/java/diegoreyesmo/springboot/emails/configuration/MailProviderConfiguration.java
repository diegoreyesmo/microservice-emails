package diegoreyesmo.springboot.emails.configuration;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

import diegoreyesmo.springboot.emails.util.Mapper;
import lombok.Data;

@Data
@Configuration
public class MailProviderConfiguration {

	@Value("${cloud.aws.ses.accessKey}")
	private String accessKey;
	@Value("${cloud.aws.ses.secretKey}")
	private String secretKey;

	@Bean
	public AmazonSimpleEmailService amazonSimpleEmailService() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
		return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(credentialsProvider)
				.withRegion(Regions.US_EAST_1).build();
	}

	@Bean
	public Random random() {
		return new Random();
	}

	@Bean
	public Mapper maper() {
		return new Mapper();
	}
}
