package diegoreyesmo.springboot.emails.task;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;
import diegoreyesmo.springboot.emails.service.MailService;
import diegoreyesmo.springboot.emails.service.NoSqlService;
import diegoreyesmo.springboot.emails.util.Mapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class SendMailTaskTests {
	@MockBean
	private MailService mailService;
	@MockBean
	private NoSqlService nosqlService;
	@MockBean
	private MailDTO mailDTO;
	@MockBean
	private Mapper mapper;

	@InjectMocks
	private SendMailTask sendMailTask;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testmailServicewillThrowEmailException() throws EmailException {
		BDDMockito.given(mailService.send(any())).willThrow(new EmailException("some error"));
		sendMailTask.run();
	}

}
