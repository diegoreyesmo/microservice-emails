package diegoreyesmo.springboot.emails.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import diegoreyesmo.springboot.emails.feign.client.NoSqlFeignClient;
import diegoreyesmo.springboot.emails.feign.response.NoSqlFeignResponse;
import diegoreyesmo.springboot.emails.feign.resquest.NoSqlRequest;

@Component
public class NoSqlCommand {

	private static final Logger log = LoggerFactory.getLogger(NoSqlCommand.class);

	@Autowired
	private NoSqlFeignClient noSqlFeignClient;

	@HystrixCommand
	public NoSqlFeignResponse create(NoSqlRequest request, String database, String collections) {
		log.info("Invocando al servicio NoSqlFeignClient.create");
		ResponseEntity<NoSqlFeignResponse> response = noSqlFeignClient.create(request, database, collections);
		return response.getBody();
	}

}
