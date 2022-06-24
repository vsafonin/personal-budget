package ru.vladimir.personalaccounter.enums;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;

public enum TypeOfTransaction {
	ADJUSTMENT("adjustment");
	
	private String name;

	private TypeOfTransaction(String name) {
		this.name = name;
	}

	public String getName() {
		//
		Locale locale = LocaleContextHolder.getLocale();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages/messages",locale);
	
		String displayString = resourceBundle.getString("typeOftransactions." + this.name);
		return displayString;
	}
	
	
	
}
