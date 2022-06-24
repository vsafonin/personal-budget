package ru.vladimir.personalaccounter.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import ru.vladimir.personalaccounter.enums.TypeOfEmails;
import ru.vladimir.personalaccounter.model.EmailModel;

@Getter
public class EmailEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    
	private final EmailModel email;
	
	private final TypeOfEmails typeOfEmails;
    
    public EmailEvent(Object source, EmailModel email, TypeOfEmails typeOfEmails) {
        super(source);
        this.email = email;
        this.typeOfEmails = typeOfEmails;
    }


}
