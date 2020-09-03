package diegoreyesmo.springboot.emails.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;
import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;
import diegoreyesmo.springboot.emails.service.MailService;
import diegoreyesmo.springboot.emails.service.NoSqlService;
import diegoreyesmo.springboot.emails.util.Mapper;
import diegoreyesmo.springboot.emails.util.Validator;
import diegoreyesmo.springboot.emails.util.Whitelist;
import lombok.extern.java.Log;

@Log
@RunWith(SpringRunner.class)
@WebMvcTest(MailController.class)
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class MailControllerTests {
	private static final String SYNC_DESCRIPTION = "[opcional] Indica si la petición se debe ejecutar de forma síncrona o no. Valor por defecto: false";

	private static final String SYNC_PARAM = "sync";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private Validator validatorUtil;

	@MockBean
	private MailService mailService;

	@MockBean
	private NoSqlService nosqlService;
	
	@MockBean
	private Mapper mapper;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Test
	public void testSendAsyncMail() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		FieldDescriptor fromFD = fieldWithPath("from").description("[requerido] Emisor del correo");
		FieldDescriptor toFD = fieldWithPath("to").description("[requerido] Lista de destinatarios");
		FieldDescriptor subjectFD = fieldWithPath("subject").description("[requerido] Asunto del correo");
		FieldDescriptor htmlContentFD = fieldWithPath("html-content")
				.description("[requerido] Contenido del correo en lenguaje HTML");
		FieldDescriptor ccFD = fieldWithPath("cc").description("[opcional] Lista de destinatarios con copia").optional()
				.type(JsonFieldType.ARRAY);
		FieldDescriptor bccFD = fieldWithPath("bcc").description("[opcional] Lista de destinatarios con copia oculta")
				.optional().type(JsonFieldType.ARRAY);
		FieldDescriptor attachmentsFD = fieldWithPath("attachments")
				.description("[opcional] Lista de archivos adjuntos").optional().type(JsonFieldType.ARRAY);
		FieldDescriptor attachmentFilenameFD = fieldWithPath("attachments.filename")
				.description("[requerido] Nombre del archivo adjunto. Ejemplo: nombre.txt").optional()
				.type(JsonFieldType.STRING);
		FieldDescriptor attachmentBase64FD = fieldWithPath("attachments.base64")
				.description(
						"[requerido] Contenido del archivo adjunto en base64, sin cabeceras. Ejemplo: bG9xdWVzZWE=")
				.optional().type(JsonFieldType.STRING);
		FieldDescriptor senderFD = fieldWithPath("sender")
				.description("[opcional] Nombre de la aplicación cliente que consume el servicio");
		FieldDescriptor statusFD = fieldWithPath("status")
				.description("Indica si la petición se ejecutó correctamente. Posibles valores: ok|fail");
		FieldDescriptor messageDF = fieldWithPath("message")
				.description("Detalle de la ejecución. Posibles valores: correo procesado | identificador del correo");

		ParameterDescriptor syncParam = parameterWithName(SYNC_PARAM).description(SYNC_DESCRIPTION).optional();
		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("send", requestParameters(syncParam),
						requestFields(fromFD, toFD, subjectFD, htmlContentFD, senderFD, ccFD, bccFD, attachmentsFD,
								attachmentFilenameFD, attachmentBase64FD),
						relaxedResponseFields(statusFD, messageDF)))
				.andDo(print());
	}
	
	@Test
	public void testSendAsyncMailThrowException() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		// Mocks
		BDDMockito.given(mailService.send(any())).willThrow(new EmailException("some error"));
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		
		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		
	}

	@Test
	public void testSendSyncMail() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("messageId", "0100016e7ef05987-941bb378-8875-4f37-bd7c-b4f6b1a364d4-000000");
		ResponseDTO response = ResponseDTO.builder().message(message).build();
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		FieldDescriptor fromFD = fieldWithPath("from").description("[requerido] Emisor del correo");
		FieldDescriptor toFD = fieldWithPath("to").description("[requerido] Lista de destinatarios");
		FieldDescriptor subjectFD = fieldWithPath("subject").description("[requerido] Asunto del correo");
		FieldDescriptor htmlContentFD = fieldWithPath("html-content")
				.description("[requerido] Contenido del correo en lenguaje HTML");
		FieldDescriptor senderFD = fieldWithPath("sender")
				.description("[opcional] Nombre de la aplicación cliente que consume el servicio");
		FieldDescriptor statusFD = fieldWithPath("status")
				.description("Indica si la petición se ejecutó correctamente. Posibles valores: ok|fail");
		FieldDescriptor messageDF = fieldWithPath("message")
				.description("Detalle de la ejecución. Posibles valores: correo procesado | identificador del correo");

		ParameterDescriptor syncParam = parameterWithName(SYNC_PARAM).description(SYNC_DESCRIPTION).optional();
		this.mockMvc
				.perform(post("/send")
						.param(SYNC_PARAM, "true").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("send-sync", requestParameters(syncParam),
						requestFields(fromFD, toFD, subjectFD, htmlContentFD, senderFD),
						relaxedResponseFields(statusFD, messageDF)))
				.andDo(print());
	}

	@Test
	public void testSendSyncMailWithoutSender() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");

		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("messageId", "0100016e7ef05987-941bb378-8875-4f37-bd7c-b4f6b1a364d4-000000");
		ResponseDTO response = ResponseDTO.builder().message(message).build();
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(
				post("/send").param(SYNC_PARAM, "true").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void testSendSyncMailThrowException() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");

		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("messageId", "0100016e7ef05987-941bb378-8875-4f37-bd7c-b4f6b1a364d4-000000");
		ResponseDTO response = ResponseDTO.builder().message(message).build();
		// Mocks
		BDDMockito.given(mailService.send(any())).willThrow(new EmailException("some error"));
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(
				post("/send").param(SYNC_PARAM, "true").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testSendAsyncMailBadRequestEmptyFrom() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(false);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestEmptyTo() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(false);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestEmptySubject() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString("no-responder@example.com")).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString("Documentación emails")).willReturn(false);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestEmptyHtml() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString("no-responder@example.com")).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString("Documentación emails")).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString("<p>Hola mundo</p>")).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestNotValidHtml() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo");
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(false);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestFromWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("nope@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), eq(Whitelist.FROM))).willReturn(false);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestToWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), eq(Whitelist.FROM))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), eq(Whitelist.TO))).willReturn(false);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestCcWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		ArrayList<String> cc = new ArrayList<>();
		cc.add("cc@example.com");
		mailDTO.setTo(to);
		mailDTO.setCc(cc);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), eq(Whitelist.FROM))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(to), eq(Whitelist.TO))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(cc), eq(Whitelist.TO))).willReturn(false);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestBccWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		ArrayList<String> from = new ArrayList<>();
		ArrayList<String> bcc = new ArrayList<>();
		ArrayList<String> cc = new ArrayList<>();
		from.add(mailDTO.getFrom());
		bcc.add("bcc@example.com");
		cc.add("cc@example.com");
		mailDTO.setTo(to);
		mailDTO.setCc(cc);
		mailDTO.setBcc(bcc);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(from), eq(Whitelist.FROM))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(to), eq(Whitelist.TO))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(cc), eq(Whitelist.TO))).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(eq(bcc), eq(Whitelist.TO))).willReturn(false);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailBadRequestAttachmentsExtensionWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		attachments.add(new AttachmentDTO());
		mailDTO.setAttachments(attachments);
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(false);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testSendAsyncMailEmptyAttachmentsExtensionWhitelist() throws Exception {
		// Dummies
		ResponseDTO response = ResponseDTO.builder().build();
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		to.add("alice@example.com");
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		mailDTO.setAttachments(attachments);
		ArrayList<String> fromList = new ArrayList<String>();
		fromList.add(mailDTO.getFrom());
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(any(), any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(false);
		ArrayList<File> attachment = new ArrayList<File>();
		attachment.add(new File("tmp"));

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(post("/send").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void testSendSyncMailNullCCWhitelistCcOk() throws Exception {
		// Dummies
		MailDTO mailDTO = new MailDTO();
		mailDTO.setFrom("no-responder@example.com");
		ArrayList<String> to = new ArrayList<>();
		ArrayList<String> from = new ArrayList<>();
		from.add(mailDTO.getFrom());
		to.add("alice@example.com");
		ArrayList<String> bcc = new ArrayList<>();
		ArrayList<String> cc = new ArrayList<>();
		bcc.add("bcc@example.com");
		cc.add("cc@example.com");
		mailDTO.setTo(to);
		mailDTO.setCc(cc);
		mailDTO.setBcc(bcc);
		mailDTO.setTo(to);
		mailDTO.setSubject("Documentación emails");
		mailDTO.setSender("JUnit");
		mailDTO.setHtmlContent("<p>Hola mundo</p>");
		ArrayList<AttachmentDTO> attachments = new ArrayList<>();
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFilename("file.txt");
		attachment.setBase64("asdf");
		attachments.add(attachment);
		mailDTO.setAttachments(attachments);
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("messageId", "0100016e7ef05987-941bb378-8875-4f37-bd7c-b4f6b1a364d4-000000");
		ResponseDTO response = ResponseDTO.builder().message(message).build();
		// Mocks
		BDDMockito.given(mailService.send(any())).willReturn(response);
		BDDMockito.given(validatorUtil.emailValidator(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyString(any())).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(to)).willReturn(true);
		BDDMockito.given(validatorUtil.validateNullOrEmptyList(cc)).willReturn(false);
		BDDMockito.given(validatorUtil.validateWhiteList(from, Whitelist.FROM)).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(to, Whitelist.TO)).willReturn(true);
		BDDMockito.given(validatorUtil.validateWhiteList(cc, Whitelist.TO)).willReturn(false);
		BDDMockito.given(validatorUtil.validateExtensions(any())).willReturn(true);
		BDDMockito.given(validatorUtil.isHtml(any())).willReturn(true);

		String content = objectMapper.writeValueAsString(mailDTO);
		log.info(content);

		this.mockMvc.perform(
				post("/send").param(SYNC_PARAM, "true").content(content).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}
