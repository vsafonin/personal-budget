package ru.vladimir.personalaccounter.exception;

public class PageNotFoundExcp extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PageNotFoundExcp(String string) {
		super(string);
	}

}
