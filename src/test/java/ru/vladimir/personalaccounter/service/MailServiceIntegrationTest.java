package ru.vladimir.personalaccounter.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.vladimir.personalaccounter.model.EmailModel;
@SpringBootTest
class MailServiceIntegrationTest {

	@Autowired
	private MailService mailService;
	
	@Test
	void test_send_message_should_be_ok() {
		
		EmailModel emailModel = new EmailModel();
		emailModel.setEmailTo("vsafonin@gmail.com");
		emailModel.setLocale(Locale.ENGLISH);
		Map<String, Object> parametrs = new HashMap<String, Object>();
		parametrs.put("displayname", "test");
		parametrs.put("activationLink", "http://localhot:8080/");
		emailModel.setParametrs(parametrs);
		
		mailService.sendRedistrationMessage(emailModel);
		
		
	}

}
