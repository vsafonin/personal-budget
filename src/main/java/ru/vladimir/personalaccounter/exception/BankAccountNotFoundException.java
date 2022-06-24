package ru.vladimir.personalaccounter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class BankAccountNotFoundException extends RuntimeException {

	public BankAccountNotFoundException(String string) {
		super(string);
	}

	public BankAccountNotFoundException() {
	}

	private static final long serialVersionUID = 1L;

}
