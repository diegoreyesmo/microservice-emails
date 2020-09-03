package diegoreyesmo.springboot.emails.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MailAuditDTO {
	private LocalDateTime eventTime;
	private String eventType;
	private Object notificationContents;
}
