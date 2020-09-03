package diegoreyesmo.springboot.emails.dto;

import java.util.List;

import lombok.Data;

@Data
public class MailDTO {
	private String sender;
	private String subject;
	private String from;
	private List<String> to;
	private List<String> cc;
	private List<String> bcc;
	private String htmlContent;
	private List<AttachmentDTO> attachments;
	private List<MailAuditDTO> audit;
}
