package diegoreyesmo.springboot.emails.feign.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import diegoreyesmo.springboot.emails.feign.response.NoSqlFeignResponse;
import diegoreyesmo.springboot.emails.feign.resquest.NoSqlRequest;

@FeignClient(name = "NO-SQL")
public interface NoSqlFeignClient {

	@PostMapping(path = "/{database}/{collections}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<NoSqlFeignResponse> create(NoSqlRequest request, @PathVariable("database") String database,
			@PathVariable("collections") String collections);
}
