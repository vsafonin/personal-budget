package ru.vladimir.personalaccounter.enums;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;
/**
 * this enum used to expose type of operations in UsrTransaction expense or income
 * @author vladimir
 *
 */
public enum TypeOfOperation {

	INCREASE("increase"),DECREASE("decrease");
	
	public String name;
	private TypeOfOperation(String name) {
		this.name = name;
	}
	
	public String getName() {
		//
		Locale locale = LocaleContextHolder.getLocale();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages/messages",locale);
	
		String displayString = resourceBundle.getString("typeOfOperations." + this.name);
		return displayString;
	}
}
