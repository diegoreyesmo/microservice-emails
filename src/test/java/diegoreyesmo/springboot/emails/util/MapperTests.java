package diegoreyesmo.springboot.emails.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;

@RunWith(SpringJUnit4ClassRunner.class)
public class MapperTests {
	private MailDTO dest;
	private MailDTO src;
	private ResponseDTO responseDTO;
	@InjectMocks
	private Mapper mapper;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		responseDTO = new ResponseDTO("ok", new HashMap<String, Object>());
		dest = new MailDTO();
		src = new MailDTO();
	}

	@Test
	public void testcopyMailDTO() {
		// Dummies
		src.setFrom("algo");
		mapper.copyMailDTO(src, dest);
		assertEquals(src.getFrom(), dest.getFrom());
	}

	@Test
	public void testmailMapping() {
		// Dummies
		src.setFrom("algo");
		mapper.mailMapping(src, responseDTO);
		assertEquals(src.getFrom(), responseDTO.getMessage().get("from"));
	}

	@Test
	public void testmailMappingWithAllFields() {
		// Dummies
		src.setFrom("algo");
		src.setSender("sender");
		List<String> cc = new ArrayList<>();
		List<String> bcc = new ArrayList<>();
		cc.add("cc@mail");
		bcc.add("bcc@mail");
		src.setCc(cc);
		src.setBcc(bcc);

		mapper.mailMapping(src, responseDTO);

		assertEquals(src.getFrom(), responseDTO.getMessage().get("from"));
		assertEquals(src.getSender(), responseDTO.getMessage().get("sender"));
		assertEquals(src.getCc(), responseDTO.getMessage().get("cc"));
		assertEquals(src.getBcc(), responseDTO.getMessage().get("bcc"));
	}
}
