package diegoreyesmo.springboot.emails.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;
import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;
import diegoreyesmo.springboot.emails.service.MailService;
import diegoreyesmo.springboot.emails.service.NoSqlService;
import diegoreyesmo.springboot.emails.task.NoSqlCreateTask;
import diegoreyesmo.springboot.emails.task.SendMailTask;
import diegoreyesmo.springboot.emails.util.Mapper;
import diegoreyesmo.springboot.emails.util.Validator;
import diegoreyesmo.springboot.emails.util.Whitelist;
import lombok.extern.java.Log;

@Log
@RestController
public class MailController {

	private static final String WRONG_ATTACHMENT_EXTENSIONS = "Todos los archivos adjuntos deben tener extensión permitida";

	private static final String WRONG_CONTENT = "Contenido html mal formado";

	private static final String MUST_HAVE_CONTENT = "Debe especificar un contenido al correo";

	private static final String MUST_HAVE_SUBJECT = "Debe especificar asunto";

	private static final String MUST_HAVE_DESTINATION = "Debe especificar al menos un destinatario";

	private static final String FROM_MUST_BE_PRESENT_IN_WHITELIST = "Debe especificar un emisor presente en lista blanca";

	private static final String MUST_HAVE_FROM = "Debe especificar un emisor";

	private static final String DETAIL_RESPONSE_FIELD = "detail";

	private static final String WHITELIST_ERROR_MESSAGE = "Todos los destinatarios deben estar en lista blanca";

	private static final String STATUS_OK = "ok";

	private static final String STATUS_FAIL = "fail";

	@Autowired
	private MailService mailService;

	@Autowired
	private NoSqlService nosqlService;

	@Autowired
	private Validator validatorUtil;
	@Autowired
	private Mapper mapper;

	@PostMapping(value = "send")
	public HttpEntity<ResponseDTO> sendMail(@RequestBody MailDTO request, @RequestParam(required = false) boolean sync)
			throws EmailException {
		ResponseEntity<ResponseDTO> response = validarRequest(request);
		if (response != null) {
			return response;
		}
		return enviarCorreo(request, sync);
	}

	/**
	 * Valida que request para enviar correo sea correcto.
	 * 
	 * @param email correo que se desea enviar.
	 * @return null si correo es válido. ResponseEntity en otro caso.
	 */
	private ResponseEntity<ResponseDTO> validarRequest(MailDTO email) {
		HashMap<String, Object> message = new HashMap<>();
		log.info("[MailController] validando nuevo correo:");
		// validar que from existe
		String from = email.getFrom();
		if (!validatorUtil.validateNullOrEmptyString(from)) {
			log.warning(MUST_HAVE_FROM);
			message.put(DETAIL_RESPONSE_FIELD, MUST_HAVE_FROM);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}
		log.info(String.format("[MailController] from: %s", from));
		// validar from contra whitelist
		ArrayList<String> fromList = new ArrayList<>();
		fromList.add(from);
		if (!validatorUtil.validateWhiteList(fromList, Whitelist.FROM)) {
			log.warning(FROM_MUST_BE_PRESENT_IN_WHITELIST);
			message.put(DETAIL_RESPONSE_FIELD, FROM_MUST_BE_PRESENT_IN_WHITELIST);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}
		
		
		// validar que existen destinatarios
		List<String> to = email.getTo();
		if (!validatorUtil.validateNullOrEmptyList(to)) {
			log.warning(MUST_HAVE_DESTINATION);
			message.put(DETAIL_RESPONSE_FIELD, MUST_HAVE_DESTINATION);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}
		log.info(String.format("[MailController] to: %s", to));
		// validar destinatarios contra lista blanca
		if (!validatorUtil.validateWhiteList(to, Whitelist.TO)) {
			log.warning(WHITELIST_ERROR_MESSAGE);
			message.put(DETAIL_RESPONSE_FIELD, WHITELIST_ERROR_MESSAGE);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar destinatarios CC contra lista blanca
		List<String> cc = email.getCc();
		if (validatorUtil.validateNullOrEmptyList(cc) && !validatorUtil.validateWhiteList(cc, Whitelist.TO)) {
			log.warning(WHITELIST_ERROR_MESSAGE);
			message.put(DETAIL_RESPONSE_FIELD, WHITELIST_ERROR_MESSAGE);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar destinatarios CC contra lista blanca
		List<String> bcc = email.getBcc();
		if (validatorUtil.validateNullOrEmptyList(bcc) && !validatorUtil.validateWhiteList(bcc, Whitelist.TO)) {
			log.warning(WHITELIST_ERROR_MESSAGE);
			message.put(DETAIL_RESPONSE_FIELD, WHITELIST_ERROR_MESSAGE);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar que existe asunto
		if (!validatorUtil.validateNullOrEmptyString(email.getSubject())) {
			log.warning(MUST_HAVE_SUBJECT);
			message.put(DETAIL_RESPONSE_FIELD, MUST_HAVE_SUBJECT);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar que existe body
		String htmlContent = email.getHtmlContent();
		if (!validatorUtil.validateNullOrEmptyString(htmlContent)) {
			log.warning(MUST_HAVE_CONTENT);
			message.put(DETAIL_RESPONSE_FIELD, MUST_HAVE_CONTENT);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar que body es un html bien formado
		if (!validatorUtil.isHtml(htmlContent)) {
			log.warning(WRONG_CONTENT);
			message.put(DETAIL_RESPONSE_FIELD, WRONG_CONTENT);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		// validar extensión de archivos adjuntos
		List<AttachmentDTO> attachments = email.getAttachments();
		if (attachments != null && !attachments.isEmpty() && !validatorUtil.validateExtensions(attachments)) {
			log.warning(WRONG_ATTACHMENT_EXTENSIONS);
			message.put(DETAIL_RESPONSE_FIELD, WRONG_ATTACHMENT_EXTENSIONS);
			return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.BAD_REQUEST);
		}

		return null;
	}

	/**
	 * Llama a servicio de email y no-sql para persistir datos.
	 * 
	 * @param email correo que se desea enviar.
	 * @param sync  true si se desea esperar que correo sea enviado (sin esperar
	 *              respaldo en no-sql). false si se desea respuesta 100% asíncrona.
	 * @return HttpEntity
	 * @throws Exception en caso de error.
	 */
	private HttpEntity<ResponseDTO> enviarCorreo(MailDTO email, boolean sync) throws EmailException {
		HashMap<String, Object> message = new HashMap<>();
		MailDTO mailDTO = new MailDTO();
		mapper.copyMailDTO(email, mailDTO);

		if (sync) {
			try {
				ResponseDTO result = mailService.send(mailDTO);
				new Thread(new NoSqlCreateTask(result, nosqlService, mailDTO, mapper)).start();
				log.info("Correo enviado");
				message.put(DETAIL_RESPONSE_FIELD, "Correo enviado");
				message.put("email", result.getMessage());
				return new ResponseEntity<>(new ResponseDTO(STATUS_OK, message), HttpStatus.OK);
			} catch (Exception e) {
				log.severe(e.getMessage());
				message.put(DETAIL_RESPONSE_FIELD, e.getMessage());
				return new ResponseEntity<>(new ResponseDTO(STATUS_FAIL, message), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			new Thread(new SendMailTask(mailService, nosqlService, mailDTO, mapper)).start();
			log.info("Correo procesado");
			message.put(DETAIL_RESPONSE_FIELD, "Correo procesado");
			return new ResponseEntity<>(new ResponseDTO(STATUS_OK, message), HttpStatus.OK);
		}
	}
}
