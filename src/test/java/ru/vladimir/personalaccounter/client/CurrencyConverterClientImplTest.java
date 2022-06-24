package ru.vladimir.personalaccounter.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class CurrencyConverterClientImplTest  {

	@Test
	void test() throws CurrencyParseExcp {
		CurrencyConverterClient client = new CurrencyConverterClientImpl();
		Currency usdCur = Currency.getInstance("USD");
		Currency rubCur = Currency.getInstance("RUB");
		assertThat(client.getConvertedCurrencySum(usdCur, rubCur, BigDecimal.TEN)).isEqualByComparingTo(BigDecimal.valueOf(561.730));
	}
	@Test
	void testFromRublToUsd() throws CurrencyParseExcp {
		CurrencyConverterClient client = new CurrencyConverterClientImpl();
		Currency usdCur = Currency.getInstance("RUB");
		Currency rubCur = Currency.getInstance("USD");
		assertThat(client.getConvertedCurrencySum(usdCur, rubCur, BigDecimal.valueOf(561.7270))).isEqualByComparingTo(BigDecimal.valueOf(10.111086));
	}
	@Test
	void testFromRublToTg() throws CurrencyParseExcp {
		CurrencyConverterClient client = new CurrencyConverterClientImpl();
		Currency usdCur = Currency.getInstance("RUB");
		Currency rubCur = Currency.getInstance("KZT");
		assertThat(client.getConvertedCurrencySum(usdCur, rubCur, BigDecimal.valueOf(561.7270))).isEqualByComparingTo(BigDecimal.valueOf(4468.538285));
	}
	@Test
	void testFromUsdToTg() throws CurrencyParseExcp {
		CurrencyConverterClient client = new CurrencyConverterClientImpl();
		Currency usdCur = Currency.getInstance("USD");
		Currency rubCur = Currency.getInstance("KZT");
		assertThat(client.getConvertedCurrencySum(usdCur, rubCur, BigDecimal.valueOf(10))).isEqualByComparingTo(BigDecimal.valueOf(4468.562150));
	}
	@Test
	void testFromRubToRub() throws CurrencyParseExcp {
		CurrencyConverterClient client = new CurrencyConverterClientImpl();
		Currency usdCur = Currency.getInstance("RUB");
		Currency rubCur = Currency.getInstance("RUB");
		assertThat(client.getConvertedCurrencySum(usdCur, rubCur, BigDecimal.valueOf(10))).isEqualByComparingTo(BigDecimal.valueOf(10));
	}

}
