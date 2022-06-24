package ru.vladimir.personalaccounter.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import ru.vladimir.personalaccounter.enums.TypeOfEmails;
import ru.vladimir.personalaccounter.service.MailService;

@Component
public class EmailEventListener implements ApplicationListener<EmailEvent> {

    
    @Autowired
    private MailService mailService;

    @Override
    public void onApplicationEvent(EmailEvent event) {
    	if (event.getTypeOfEmails() == TypeOfEmails.CHANGED_EMAIL) {
    		mailService.sendChangedActivationEmail(event.getEmail());
    	}
    	if (event.getTypeOfEmails() == TypeOfEmails.REGISTRATION_EMAIL) {
    		mailService.sendRedistrationMessage(event.getEmail());
    	}
    	if (event.getTypeOfEmails() == TypeOfEmails.PASSWORD_RECOVER_EMAIL) {
    		mailService.sendPasswordRecoveryEmail(event.getEmail());
    	}
    	   
     
    }

}
