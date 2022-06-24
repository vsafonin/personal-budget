package ru.vladimir.personalaccounter.exception;

public class BadAppUserFormatException extends Exception {

    private static final long serialVersionUID = 1L;

	public BadAppUserFormatException(String appUser_is_null) {
        super(appUser_is_null);
    }
    
}
