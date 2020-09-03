package diegoreyesmo.springboot.emails.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import diegoreyesmo.springboot.emails.dto.AttachmentDTO;

@Component
public class Validator {

	public static final String START_TAG = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";
	public static final String END_TAG = "\\</\\w+\\>";
	public static final String SELF_CLOSING_TAG = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";
	public static final String HTML_ENTITY = "&[a-zA-Z][a-zA-Z0-9]+;";
	public static final Pattern HTML_PATTERN = Pattern.compile(
			"(" + START_TAG + ".*" + END_TAG + ")|(" + SELF_CLOSING_TAG + ")|(" + HTML_ENTITY + ")", Pattern.DOTALL);
	public static final String MAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	@Value("${emails.whiteList.extensions}")
	private String[] supportedExtensions;

	@Value("${emails.whiteList.from}")
	private String[] whiteListFrom;

	@Value("${emails.whiteList.to}")
	private String[] whiteListTo;

	/**
	 * Valida regex de email.
	 * 
	 * @param email correo que se desea validar.
	 * @return true si cumple con el regex de un correo.
	 */
	public boolean emailValidator(String email) {
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(MAIL_REGEX);
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * 
	 * @param list
	 * @return true si la lista es distinta de null o vacío.
	 */
	public boolean validateNullOrEmptyList(List<String> list) {
		return (list != null && !list.isEmpty());

	}

	/**
	 * 
	 * @param string
	 * @return true si el string es distinto de null o vacío.
	 */
	public boolean validateNullOrEmptyString(String string) {
		return (string != null && !string.trim().isEmpty());
	}

	/**
	 * retorna true si el string contiene tags html
	 *
	 * @param String
	 * @return true si el string contiene HTML
	 */
	public boolean isHtml(String s) {
		boolean ret = false;
		if (s != null) {
			ret = HTML_PATTERN.matcher(s).find();
		}
		return ret;
	}

	/**
	 * Valida que la lista de archivos adjuntos tenga extensiones permitidas.
	 * 
	 * @param attachments lista de archivos adjuntos.
	 * @return true si todos los adjuntos cumplen con las extensiones permitidas.
	 *         false en otro caso.
	 * 
	 */
	public boolean validateExtensions(List<AttachmentDTO> attachments) {
		for (AttachmentDTO attachment : attachments) {
			String filename = attachment.getFilename();
			int extensionIndex = filename.lastIndexOf('.') + 1;
			String fileExtension = filename.substring(extensionIndex);
			List<String> supportedExtensionsList = Arrays.asList(supportedExtensions);
			if (!supportedExtensionsList.contains(fileExtension)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Valida que una lista de correos (origen o destinatario) pertenesca a la lista
	 * blanca.
	 * 
	 * @param emails    lista de correos a validar.
	 * @param whitelist lista blanca contra la que se desea validar.
	 * @return true si emails es un subconjunto de whitelist. false en otro caso.
	 * 
	 */
	public boolean validateWhiteList(List<String> emails, Whitelist whitelist) {
		if (whitelist == null || emails == null) {
			return false;
		}

		List<String> whiteList = null;
		if (whitelist.equals(Whitelist.FROM)) {
			whiteList = Arrays.asList(whiteListFrom);
		} else {
			whiteList = Arrays.asList(whiteListTo);
		}

		if (whiteList.get(0).equals("any")){
			return true;
		}

		for (String email : emails) {
			String cleanEmail = cleanTag(email);
			if (whitelist.equals(Whitelist.TO)) {
				cleanEmail = getDomain(email);
			}			
			if (!whiteList.contains(cleanEmail)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Elimina el tag del correo. Por ejemplo: "Bob" <bob@example.com> lo convierte
	 * en bob@example.com.
	 * 
	 * @param email posiblemente con etiquieta en formato "Bob" <bob@example.com>.
	 * @return email sin tag
	 */
	private String cleanTag(String email) {
		int emailIndex = email.lastIndexOf(' ');
		emailIndex = emailIndex == -1 ? 0 : emailIndex + 1;
		String emailWithAngleBracket = email.substring(emailIndex);
		String withoutTag = emailWithAngleBracket.replace("<", "").replace(">", "");
		String lowerCase = withoutTag.toLowerCase();
		return lowerCase.trim();
	}

	/**
	 * Elimina la parte antes del @ (incluido).
	 * 
	 * @param email completo.
	 * @return dominio del email.
	 */
	private String getDomain(String email) {
		int atIndex = email.lastIndexOf('@') + 1;
		String domain = email.substring(atIndex);
		return domain.toLowerCase();
	}

}
