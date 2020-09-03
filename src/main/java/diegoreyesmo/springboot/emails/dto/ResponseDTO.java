package diegoreyesmo.springboot.emails.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String status;
	private Map<String,Object> message;

}
