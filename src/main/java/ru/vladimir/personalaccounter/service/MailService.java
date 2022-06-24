package ru.vladimir.personalaccounter.service;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import ru.vladimir.personalaccounter.event.EmailEventListener;
import ru.vladimir.personalaccounter.model.EmailModel;

@Service
public class MailService {

	private static final Logger LOG = Logger.getLogger(EmailEventListener.class.getName());

	@Autowired
	@Qualifier("emailTemplateEngine")
	private TemplateEngine templateEngine;
	
	@Value("${mail.server.from}")
	private String emailFrom;
	
	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	@Qualifier("emailMessageSource")
	private MessageSource messageSource;

	public void sendRedistrationMessage(EmailModel emailModel) {
		sendEmail(emailModel, "register-succses", "registration.info.subject");
	}
	
	/**
	 * when user changing email, before save this email, we are check it by sending activation email
	 * @param email
	 */
	public void sendChangedActivationEmail(EmailModel emailModel) {
		sendEmail(emailModel, "changed-email-confirm", "confirm.changed.email.info.text");
	}
	

	public void sendPasswordRecoveryEmail(EmailModel emailModel) {
		sendEmail(emailModel, "password-chage-email", "recoverpassword.email.info");
	}
	
	private void sendEmail(EmailModel emailModel, String template, String message) {
		try {
			// event.sendRegistrationMessage();
			Context context = new Context();
			context.setVariable("displayname", emailModel.getParametrs().get("displayname"));
			context.setVariable("activationLink", emailModel.getParametrs().get("activationLink"));
			String process = templateEngine.process(template, context);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,
					MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
			messageHelper
					.setSubject(messageSource.getMessage(message, null, emailModel.getLocale()));
			messageHelper.setText(process,true);
			messageHelper.setTo(emailModel.getEmailTo());
			messageHelper.setFrom(emailFrom);
			Runnable emailTask = new Runnable() {
				
				@Override
				public void run() {
					mailSender.send(mimeMessage);
				}
			};
			Thread newThreadFormEmailTask = new Thread(emailTask);
			newThreadFormEmailTask.start();
			//TODO подумать над тем как организовать точную доставку сообщений нужен какой то лист который переодически 
			//шлет сообщения
			
			
		} catch (MessagingException exp) {
			LOG.warning(">>>>>>> Messaging exception, check mail sender");
		}
	}


}
