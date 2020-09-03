package diegoreyesmo.springboot.emails.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;
import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class MailServiceImplTests {

	@MockBean
	private AmazonSimpleEmailService client;
	@MockBean
	private Random random;
	@MockBean
	private RawMessage rawMessage;
	@MockBean
	private SendRawEmailRequest sendRawEmailRequest;
	@MockBean
	private ByteArrayOutputStream outputStream;
	@MockBean
	private Map<String, Object> mapSdkHttpMetadata;
	@MockBean
	private SendRawEmailResult sendRawEmailResult;
	@MockBean
	private SdkHttpMetadata sdkHttpMetadata;
	@InjectMocks
	private MailServiceImpl mailServiceImpl;
	@MockBean
	private MimeMessage mimeMessage;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		BDDMockito.given(sendRawEmailResult.getMessageId()).willReturn("dummyMessageId");
		BDDMockito.given(client.sendRawEmail(any())).willReturn(sendRawEmailResult);
		BDDMockito.given(outputStream.toByteArray()).willReturn(new byte[1]);
		BDDMockito.given(sendRawEmailResult.getSdkHttpMetadata()).willReturn(sdkHttpMetadata);
	}

	@Test
	public void testSendMail() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<String>();
		to.add("vrd18699@endtest.io");
		mailDTO.setSubject("test");
		mailDTO.setTo(to);
		mailDTO.setCc(to);
		mailDTO.setBcc(to);
		mailDTO.setHtmlContent("<p>content</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setBase64("anVuaXQ=");
		attachment.setFilename("filename.txt");
		attachments.add(attachment);
		mailDTO.setAttachments(attachments);

		ResponseDTO sendResponse = mailServiceImpl.send(mailDTO);
		assertTrue(sendResponse.getStatus().equals("ok"));

	}

	@Test(expected = EmailException.class)
	public void testSendMailNullParamsFrom() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailServiceImpl.send(mailDTO);
	}
	
	@Test(expected = EmailException.class)
	public void testSendMailNullParamsHtmlContent() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("a@a.a");
		mailServiceImpl.send(mailDTO);
	}
	
	@Test(expected = EmailException.class)
	public void testSendMailNullParamsTo() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("a@a.a");
		mailDTO.setHtmlContent("");
		mailServiceImpl.send(mailDTO);
	}
	
	@Test
	public void testSendMailNullParamsCc() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("a@a.a");
		mailDTO.setTo(new ArrayList<>());
		mailDTO.setHtmlContent("");
		
		ResponseDTO sendResponse = mailServiceImpl.send(mailDTO);
		assertTrue(sendResponse.getStatus().equals("ok"));
	}
	
	@Test(expected = EmailException.class)
	public void testSendMailNullFilename() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<String>();
		to.add("vrd18699@endtest.io");
		mailDTO.setSubject("test");
		mailDTO.setTo(to);
		mailDTO.setCc(to);
		mailDTO.setBcc(to);
		mailDTO.setHtmlContent("<p>content</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setBase64("anVuaXQ=");
		attachment.setFilename("");
		attachments.add(attachment);
		mailDTO.setAttachments(attachments);

		mailServiceImpl.send(mailDTO);
	}

	@Test
	public void testSendMailNullAwsResponse() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<String>();
		to.add("vrd18699@endtest.io");
		mailDTO.setSubject("test");
		mailDTO.setTo(to);
		mailDTO.setCc(to);
		mailDTO.setBcc(to);
		mailDTO.setHtmlContent("<p>content</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setBase64("anVuaXQ=");
		attachment.setFilename("filename.txt");
		attachments.add(attachment);
		mailDTO.setAttachments(attachments);
		BDDMockito.given(client.sendRawEmail(any())).willReturn(null);
		assertEquals(null, mailServiceImpl.send(mailDTO));
	}

	@Test
	public void testSendMailNullAwsMessageId() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<String>();
		to.add("vrd18699@endtest.io");
		mailDTO.setSubject("test");
		mailDTO.setTo(to);
		mailDTO.setCc(to);
		mailDTO.setBcc(to);
		mailDTO.setHtmlContent("<p>content</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setBase64("anVuaXQ=");
		attachment.setFilename("filename.txt");
		attachments.add(attachment);
		mailDTO.setAttachments(attachments);
		BDDMockito.given(sendRawEmailResult.getMessageId()).willReturn(null);
		assertEquals("null", mailServiceImpl.send(mailDTO).getMessage().get("message-id"));
	}
}
