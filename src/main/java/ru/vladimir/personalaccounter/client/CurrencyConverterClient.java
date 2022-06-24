package ru.vladimir.personalaccounter.client;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Classes that implements this interface should return
 * the converted amount in the required currency using an external service  
 *  
 * @author Vladimir Afonin
 *
 */

public interface CurrencyConverterClient {

	/**
	 * Converting one currency into another
	 * @param currencyIN - source currency for conversion
	 * @param currencyOUT - output currency, result conversion
	 * @param sum - the amount what we are going to convert
	 * @return - the amount is result of conversion
	 * @throws CurrencyParseExcp - if class can't parse message from an external api, this client would throw it
	 */
	BigDecimal getConvertedCurrencySum(Currency currencyIN, Currency currencyOUT, BigDecimal sum) throws CurrencyParseExcp;
}
