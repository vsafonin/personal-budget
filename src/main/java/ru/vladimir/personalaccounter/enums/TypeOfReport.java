package ru.vladimir.personalaccounter.enums;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.i18n.LocaleContextHolder;

public enum TypeOfReport {
	PURCHASE_CATEGORY("purchaseByCategories"),PURCHASE_PRODUCT("purchaseByProduct"),SALARY_INCREASE("allIncreaseSalaryByPartner");
	
	private String nameOfReport;
	
	private TypeOfReport(String nameOfReport) {
		this.nameOfReport = nameOfReport;
	}

	public String getNameOfReport() {
		//
		Locale locale = LocaleContextHolder.getLocale();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("messages/messages",locale);
		
		String nameOfReport = resourceBundle.getString("reports." + this.nameOfReport);
		return nameOfReport;
	}	
}
