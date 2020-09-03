package diegoreyesmo.springboot.emails.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;
import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;
import diegoreyesmo.springboot.emails.service.MailService;
import lombok.extern.java.Log;

@Log
@Service
public class MailServiceImpl implements MailService {

	public static final int FILE_NAME_LENGTH = 10;
	@Autowired
	private AmazonSimpleEmailService client;

	@Override
	public ResponseDTO send(MailDTO mailDTO) throws EmailException {
		List<File> files = new ArrayList<>();
		try {
			Session session = Session.getDefaultInstance(new Properties());
			MimeMessage message = new MimeMessage(session);
			MimeMultipart body = new MimeMultipart("mixed");
			String subject = mailDTO.getSubject();
			if (subject != null)
				message.setSubject(subject, "UTF-8");

			String from = mailDTO.getFrom();
			if (from == null)
				throw new EmailException("null from");
			message.setFrom(new InternetAddress(from));

			String htmlContent = mailDTO.getHtmlContent();
			if (htmlContent == null)
				throw new EmailException("null html-content");
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(htmlContent, "text/html; charset=UTF-8");
			textPart.setHeader("Content-Transfer-Encoding", "base64");
			body.addBodyPart(textPart);

			List<String> to = mailDTO.getTo();
			if (to == null)
				throw new EmailException("null to");
			message.setRecipients(RecipientType.TO,
					Arrays.stream(to.toArray(new String[to.size()])).collect(Collectors.joining(", ")));

			List<String> cc = mailDTO.getCc();
			if (cc != null)
				message.addRecipients(RecipientType.CC,
						Arrays.stream(cc.toArray(new String[cc.size()])).collect(Collectors.joining(",")));

			List<String> bcc = mailDTO.getBcc();
			if (bcc != null)
				message.addRecipients(RecipientType.BCC,
						Arrays.stream(bcc.toArray(new String[bcc.size()])).collect(Collectors.joining(",")));

			List<AttachmentDTO> attachments = mailDTO.getAttachments();
			if (attachments != null) {
				for (AttachmentDTO attachment : attachments) {
					String fileName = attachment.getFilename();
					// create output file
					File outputFile = new File("_" + RandomStringUtils.randomAlphabetic(FILE_NAME_LENGTH));
					files.add(outputFile);
					// decode the string and write to file
					byte[] decodedBytes = Base64.getDecoder().decode(attachment.getBase64());
					FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
					DataSource ds = new FileDataSource(outputFile);

					if (!fileName.isEmpty()) {
						// Agrega Documento como adjunto
						MimeBodyPart filePart = new MimeBodyPart();
						filePart.setFileName(fileName);
						filePart.setDescription(fileName, "UTF-8");
						filePart.setDataHandler(new DataHandler(ds));
						body.addBodyPart(filePart);
					} else {
						throw new EmailException("El nombre de cada archivo adjunto es un campo obligatorio");
					}
				}
			}
			message.setContent(body);
			log.info("[sendRawEmail] Attempting to send an email through Amazon SES using the AWS SDK for Java...");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);

			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
			SendRawEmailRequest sendRawEmailRequest = new SendRawEmailRequest(rawMessage);
			for (File file : files) {
				Files.delete(file.toPath());
			}
			SendRawEmailResult result = client.sendRawEmail(sendRawEmailRequest);
			log.info("Email sent!");
			return convertirResponseDTO(result);
		} catch (Exception e) {
			throw new EmailException(e.getMessage(), e);
		}
	}

	private ResponseDTO convertirResponseDTO(SendRawEmailResult result) {
		if (result == null)
			return null;
		HashMap<String, Object> message = new HashMap<>();
		String messageId = result.getMessageId();
		if (messageId == null) {
			messageId = "null";
		}
		message.put("message-id", new String(messageId.getBytes(), StandardCharsets.UTF_8));
		return ResponseDTO.builder().message(message).status("ok").build();
	}

}
