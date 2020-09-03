package diegoreyesmo.springboot.emails.feign.resquest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NoSqlRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Map<String, Object>> newDocuments;

}
