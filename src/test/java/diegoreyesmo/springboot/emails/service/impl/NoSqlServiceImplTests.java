package diegoreyesmo.springboot.emails.service.impl;

import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.base.MockitoException;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import diegoreyesmo.springboot.emails.dto.DatabaseDTO;
import diegoreyesmo.springboot.emails.feign.resquest.NoSqlRequest;
import diegoreyesmo.springboot.emails.hystrix.NoSqlCommand;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class NoSqlServiceImplTests {

	@MockBean
	private NoSqlCommand noSqlCommand;

	@MockBean
	private DatabaseDTO databaseDTO;

	@MockBean
	private NoSqlRequest nosqlRequest;

	@InjectMocks
	private NoSqlServiceImpl noSqlServiceImpl;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCreate() throws Exception {
		Map<String, Object> mail = new HashMap<>();
		noSqlServiceImpl.create(mail);
	}

	@Test
	public void testCreateException() throws Exception {
		Map<String, Object> mail = new HashMap<>();
//		nosqlRequest
		BDDMockito.given(noSqlCommand.create(any(), any(), any())).willThrow(MockitoException.class);
		noSqlServiceImpl.create(mail);
	}
}
