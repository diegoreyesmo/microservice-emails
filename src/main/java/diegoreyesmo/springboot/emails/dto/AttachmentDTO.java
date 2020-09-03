package diegoreyesmo.springboot.emails.dto;

import lombok.Data;

@Data
public class AttachmentDTO {
	private String filename;
	private String base64;
}
