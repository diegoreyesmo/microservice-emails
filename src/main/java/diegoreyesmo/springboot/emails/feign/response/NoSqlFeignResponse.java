package diegoreyesmo.springboot.emails.feign.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class NoSqlFeignResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean ok;
	private List<Map<String, Object>> documents;
}
