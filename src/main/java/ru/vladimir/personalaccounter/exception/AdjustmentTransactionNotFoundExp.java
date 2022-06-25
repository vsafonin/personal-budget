package ru.vladimir.personalaccounter.exception;

public class AdjustmentTransactionNotFoundExp extends RuntimeException {

	public AdjustmentTransactionNotFoundExp(String string) {
		super(string);
	}

	public AdjustmentTransactionNotFoundExp() {
	}

	private static final long serialVersionUID = 1L;

}
