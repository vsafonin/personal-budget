package ru.vladimir.personalaccounter.client;

/**
 * If classes which implements CurrencyConverterClient can't convert currency
 * they throw this exception
 * @author Vladimir Afonin
 *
 */
public class CurrencyParseExcp extends Exception {

	public CurrencyParseExcp(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
