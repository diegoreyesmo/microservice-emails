package diegoreyesmo.springboot.emails.task;

import org.springframework.beans.factory.annotation.Autowired;

import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.service.NoSqlService;
import diegoreyesmo.springboot.emails.util.Mapper;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

@Log
@AllArgsConstructor
public class NoSqlCreateTask implements Runnable {
	private ResponseDTO result;
	private NoSqlService nosqlService;
	private MailDTO mailDTO;
	@Autowired
	private Mapper mapper;

	@Override
	public void run() {
		log.info(result.toString());
		mapper.mailMapping(mailDTO, result);
		nosqlService.create(result.getMessage());
	}

}
