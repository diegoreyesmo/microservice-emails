package diegoreyesmo.springboot.emails.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import diegoreyesmo.springboot.emails.dto.DatabaseDTO;
import diegoreyesmo.springboot.emails.feign.resquest.NoSqlRequest;

@Configuration
public class NoSqlConfiguration {

	@Value("${nosql.database}")
	private String database;
	@Value("${nosql.collection}")
	private String collection;
	@Value("${nosql.pageNum}")
	private Integer pageNum;
	@Value("${nosql.pageSize}")
	private Integer pageSize;

	@Bean
	public DatabaseDTO databaseDTO() {
		return DatabaseDTO.builder().name(database).collection(collection).build();
	}

	@Bean
	public NoSqlRequest nosqlRequest() {
		return NoSqlRequest.builder().newDocuments(null).build();
	}
}
