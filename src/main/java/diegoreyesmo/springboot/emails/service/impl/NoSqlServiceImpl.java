package diegoreyesmo.springboot.emails.service.impl;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import diegoreyesmo.springboot.emails.dto.DatabaseDTO;
import diegoreyesmo.springboot.emails.feign.resquest.NoSqlRequest;
import diegoreyesmo.springboot.emails.hystrix.NoSqlCommand;
import diegoreyesmo.springboot.emails.service.NoSqlService;
import lombok.extern.java.Log;

@Log
@Service
public class NoSqlServiceImpl implements NoSqlService {
	@Autowired
	private NoSqlCommand noSqlCommand;

	@Autowired
	private DatabaseDTO databaseDTO;

	@Autowired
	private NoSqlRequest nosqlRequest;

	@Override
	public void create(Map<String, Object> mail) {
		ArrayList<Map<String, Object>> newDocuments = new ArrayList<>();
		newDocuments.add(mail);
		nosqlRequest.setNewDocuments(newDocuments);
		try {
			noSqlCommand.create(nosqlRequest, databaseDTO.getName(), databaseDTO.getCollection());
		} catch (Exception e) {
			log.severe(e.getMessage());
		}

	}

}
