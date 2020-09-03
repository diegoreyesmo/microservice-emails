package diegoreyesmo.springboot.emails.service;

import diegoreyesmo.springboot.emails.dto.MailDTO;
import diegoreyesmo.springboot.emails.dto.ResponseDTO;
import diegoreyesmo.springboot.emails.exception.EmailException;

public interface MailService {

	ResponseDTO send(MailDTO mailDTO) throws EmailException;

}
