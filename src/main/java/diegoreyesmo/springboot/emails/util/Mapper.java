package diegoreyesmo.springboot.emails.util;

import java.util.List;

import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Mapper {

	/**
	 * Clona objeto MailDTO.
	 * 
	 * @param src  origen.
	 * @param dest destino.
	 */
	public void copyMailDTO(MailDTO src, MailDTO dest) {
		dest.setAttachments(src.getAttachments());
		dest.setAudit(src.getAudit());
		dest.setCc(src.getCc());
		dest.setBcc(src.getBcc());
		dest.setFrom(src.getFrom());
		dest.setHtmlContent(src.getHtmlContent());
		dest.setSender(src.getSender());
		dest.setSubject(src.getSubject());
		dest.setTo(src.getTo());
	}

	/**
	 * 
	 * @param mailDTO
	 * @param result
	 */
	public void mailMapping(MailDTO mailDTO, ResponseDTO result) {
		String sender = mailDTO.getSender();
		if (sender != null)
			result.getMessage().put("sender", sender);
		String from = mailDTO.getFrom();
		result.getMessage().put("from", from);
		List<String> to = mailDTO.getTo();
		result.getMessage().put("to", to);
		String subject = mailDTO.getSubject();
		result.getMessage().put("subject", subject);
		List<String> cc = mailDTO.getCc();
		if (cc != null)
			result.getMessage().put("cc", cc);
		List<String> cco = mailDTO.getBcc();
		if (cco != null)
			result.getMessage().put("bcc", cco);
	}
}
