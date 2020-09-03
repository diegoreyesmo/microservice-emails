package diegoreyesmo.springboot.emails.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class ValidatorTests {

	@InjectMocks
	private Validator validatorUtil;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testEmailValidatorValid() throws Exception {
		assertTrue(validatorUtil.emailValidator("a@a.com"));
	}

	@Test
	public void testEmailValidatorInvalid() throws Exception {
		assertFalse(validatorUtil.emailValidator("aa.com"));
	}

	@Test
	public void testHtmlValidatorValid() throws Exception {
		assertTrue(validatorUtil.isHtml("<p>valid html</p>"));
	}

	@Test
	public void testHtmlValidatorInValid() throws Exception {
		assertFalse(validatorUtil.isHtml("<p>valid html"));
	}

	@Test
	public void testHtmlValidatorInValidNull() throws Exception {
		assertFalse(validatorUtil.isHtml(null));
	}

	@Test
	public void testValidateNullOrEmptyListValid() throws Exception {
		ArrayList<String> list = new ArrayList<>();
		list.add("algo");
		assertTrue(validatorUtil.validateNullOrEmptyList(list));
	}

	@Test
	public void testValidateNullOrEmptyListInValidNull() throws Exception {
		assertFalse(validatorUtil.validateNullOrEmptyList(null));
	}

	@Test
	public void testValidateNullOrEmptyListInValidEmpty() throws Exception {
		assertFalse(validatorUtil.validateNullOrEmptyList(new ArrayList<>()));
	}

	@Test
	public void testValidateNullOrEmptyStringValid() throws Exception {
		assertTrue(validatorUtil.validateNullOrEmptyString("algo"));
	}

	@Test
	public void testValidateNullOrEmptyStringInValidEmpty() throws Exception {
		assertFalse(validatorUtil.validateNullOrEmptyString(""));
	}

	@Test
	public void testValidateNullOrEmptyStringInValidNull() throws Exception {
		assertFalse(validatorUtil.validateNullOrEmptyString(null));
	}

	@Test
	public void testValidateExtensionsValid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "supportedExtensions", new String[] { "txt", "doc" });
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFilename("file.txt");
		attachments.add(attachment);
		assertTrue(validatorUtil.validateExtensions(attachments));
	}

	@Test
	public void testValidateExtensionsInvalid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "supportedExtensions", new String[] { "txt", "doc" });
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFilename("file.exe");
		attachments.add(attachment);
		assertFalse(validatorUtil.validateExtensions(attachments));
	}

	@Test
	public void testValidateWhiteListFromValid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListFrom", new String[] { "no-reply@example.com" });
		List<String> emails = new ArrayList<>();
		emails.add("no-reply@example.com");
		assertTrue(validatorUtil.validateWhiteList(emails, Whitelist.FROM));
	}
	
	@Test
	public void testValidateWhiteListFromValidWithNameTag() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListFrom", new String[] { "no-reply@example.com" });
		List<String> emails = new ArrayList<>();
		emails.add("<Emisor del Correo> no-reply@example.com");
		assertTrue(validatorUtil.validateWhiteList(emails, Whitelist.FROM));
	}


	@Test
	public void testValidateWhiteListFromInvalid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListFrom", new String[] { "no-reply@example.com" });
		List<String> emails = new ArrayList<>();
		emails.add("invalid@example.com");
		assertFalse(validatorUtil.validateWhiteList(emails, Whitelist.FROM));
	}

	@Test
	public void testValidateWhiteListInvalidNullListParam() throws Exception {
		assertFalse(validatorUtil.validateWhiteList(null, Whitelist.FROM));
	}

	@Test
	public void testValidateWhiteListInvalidNullWhiteParam() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListFrom", new String[] { "no-reply@example.com" });
		List<String> emails = new ArrayList<>();
		emails.add("invalid@example.com");
		assertFalse(validatorUtil.validateWhiteList(emails, null));
	}

	@Test
	public void testValidateWhiteListToValid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListTo", new String[] { "example.com" });
		List<String> emails = new ArrayList<>();
		emails.add("no-reply@example.com");
		assertTrue(validatorUtil.validateWhiteList(emails, Whitelist.TO));
	}
	
	@Test
	public void testValidateWhiteListToValidAny() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListTo", new String[] { "any" });
		List<String> emails = new ArrayList<>();
		emails.add("no-reply@example.com");
		assertTrue(validatorUtil.validateWhiteList(emails, Whitelist.TO));
	}


	@Test
	public void testValidateWhiteListToInvalid() throws Exception {
		ReflectionTestUtils.setField(validatorUtil, "whiteListTo", new String[] { "endtest.io" });
		List<String> emails = new ArrayList<>();
		emails.add("client@example.com");
		assertFalse(validatorUtil.validateWhiteList(emails, Whitelist.TO));
	}

}
