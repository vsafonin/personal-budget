package ru.vladimir.personalaccounter.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import javax.mail.internet.ParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Currency converter client. This class implement CurrencyConverterClient interface.
 * Using Central Bank of Russia open api for get current currency. 
 * 1. Get sum in currency what user use.
 * 2. Converts it to rubles
 * 3. Converts to currency what need user want
 * Return result of conversion.
 * @author Vladimir Afonin
 * @see CurrencyConverterClient.java
 *
 */

@Component
public class CurrencyConverterClientImpl implements CurrencyConverterClient {
	private final String CBRF_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";
	private final String RUB = "RUB";
	private final String WE_CANT_PARSE_FILE_FROM_CBRF="can't parse from cbrf";
	private final String CBRF_XML_FILE_TAG_NAME_CURRENCY = "CharCode";
	private final String CBRF_XML_FILE_TAG_RUBL_NOMINAL = "Nominal";
	private final String CBRF_XML_FILE_TAG_RUBL_RATE = "Value";

	@Override
	public BigDecimal getConvertedCurrencySum(Currency currencyIN, Currency currencyOUT, BigDecimal sum) throws CurrencyParseExcp {
		
		if (currencyIN.equals(currencyOUT)) {
			//nothing to do
			return sum;
		}
		BigDecimal sumOut = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(CBRF_API_URL);

			Node root = document.getDocumentElement();
			NodeList currencysFromDocument = root.getChildNodes();
			
			BigDecimal sumIn;
			if (currencyIN.getCurrencyCode().equals(RUB)){
				sumIn = sum; //we don't need convert to Rubles
			}else {
				sumIn = convertToRubls(currencysFromDocument, currencyIN, sum);
			}
			
			if(currencyOUT.getCurrencyCode().equals(RUB)) {
				sumOut = sumIn; //we don't need convert to Rubles
			}
			else {
				sumOut = convertFromRubls(currencysFromDocument, currencyOUT, sumIn);
			}
		} catch (Exception exp) {
			throw new CurrencyParseExcp(WE_CANT_PARSE_FILE_FROM_CBRF);
			
		}

		return sumOut;
	}
	
	/**
	 * convert amount in rubles to another currency
	 * @param currenciesFromDocument - list of currencies in the NodeList format
	 * @param currencyOUT -the currency what we want to convert
	 * @param sum - the amount in rubles what we want to convert
	 * @return - the amount is result of conversion
	 * @throws ParseException - if cbrf changes out file and rename parameter we can't parse this file,
	 *  and we will get this exception
	 */
	private BigDecimal convertFromRubls(NodeList currenciesFromDocument, Currency currencyOUT, BigDecimal sum) throws ParseException {
		CurrencyNominalAndRate nominalAndRate = new CurrencyNominalAndRate(currenciesFromDocument, currencyOUT);
		BigDecimal currentRubleExchangeRate =  nominalAndRate.nominal.divide(nominalAndRate.rate,3,RoundingMode.HALF_UP);
		BigDecimal result = sum.multiply(currentRubleExchangeRate);
		return result;
	}
	
	/**
	 * converts amount in another currency to rubles
	 * @param currenciesFromDocument - list of currencies in the NodeList format
	 * @param currencyFromConvert - the currency what we want to convert
	 * @param sum - the amount in another currency what we want to convert
	 * @return - the amount is result of conversion
	 * @throws ParseException if cbrf changes out file and rename parameter we can't parse this file,
	 *  and we will get this exception
	 */
	private BigDecimal convertToRubls(NodeList currenciesFromDocument, Currency currencyFromConvert, BigDecimal sum) throws ParseException {
			CurrencyNominalAndRate currencyNominalAndRate = new CurrencyNominalAndRate(currenciesFromDocument, currencyFromConvert);
			BigDecimal currentWantedCurrencyRate = currencyNominalAndRate.rate.divide(currencyNominalAndRate.nominal,3,RoundingMode.HALF_UP);
			BigDecimal result = sum.multiply(currentWantedCurrencyRate);
			return result;
	}
	
	
	/**
	 * utility inner class for get nominal of rubles from cbrf xml file
	 * and exchange rate to the desired currency
	 * @author Vladimir Afonin
	 *
	 */
	private class CurrencyNominalAndRate{
		
		private BigDecimal nominal;
		private BigDecimal rate;
		
		public CurrencyNominalAndRate(NodeList currenciesFromDocument, Currency currencyFromConvert) throws ParseException {
			try {
				for (int i = 0; i < currenciesFromDocument.getLength(); i++) {
					if (nominal != null && rate != null) {
						break;
					}
					Node rootElementCurrency = currenciesFromDocument.item(i);
					if (rootElementCurrency.getNodeType() != Node.TEXT_NODE) {
						NodeList chaildElementCurrency = rootElementCurrency.getChildNodes();
						for (int j = 0; j < chaildElementCurrency.getLength(); j++) {
							Node currency = chaildElementCurrency.item(j);
							if (currency.getNodeName().equals(CBRF_XML_FILE_TAG_NAME_CURRENCY)) {
								if (currency.getTextContent().equals(currencyFromConvert.getCurrencyCode())) {
									while (j < chaildElementCurrency.getLength()) {
										if (chaildElementCurrency.item(j).getNodeName().equals(CBRF_XML_FILE_TAG_RUBL_NOMINAL)) {
											this.nominal = new BigDecimal(chaildElementCurrency.item(j).getTextContent()); //if cbrf change document we got Number format exp
										}
										if (chaildElementCurrency.item(j).getNodeName().equals(CBRF_XML_FILE_TAG_RUBL_RATE)){
											this.rate = new BigDecimal(chaildElementCurrency.item(j).getTextContent().replace(',', '.')); //if cbrf change document we got Number format exp
										}
										j++;
									}
									if (nominal == null || rate == null) throw new ParseException(); //if cbrf change document structure, we got exp
							}
						}
					}
			}
			
				
			}
		}
			catch (NumberFormatException e) {
				throw new ParseException();
			}

		}
		
		
	}
}
