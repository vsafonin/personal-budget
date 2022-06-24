package ru.vladimir.personalaccounter.exception;

public class UsrTransactionNotFoundExp extends RuntimeException {

	public UsrTransactionNotFoundExp(String string) {
		super(string);
	}

	public UsrTransactionNotFoundExp() {
	}

	private static final long serialVersionUID = 1L;

}
