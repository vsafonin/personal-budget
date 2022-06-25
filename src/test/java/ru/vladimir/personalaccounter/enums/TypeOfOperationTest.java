package ru.vladimir.personalaccounter.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;

class TypeOfOperationTest {
	
	@MockBean
	private MessageSource messageSource;
	
	/*
	 * тестирую что работает метод getName у Enum и работает перевод на другой язык
	 */
	@Test
	void testGetName() {
		Locale.setDefault(new Locale("en", "EN"));

		assertThat(TypeOfOperation.DECREASE.getName()).isEqualTo("Decrease");
	}

}
