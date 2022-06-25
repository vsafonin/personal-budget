package ru.vladimir.personalaccounter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.exception.DebtTransactionNotFoundExp;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@WebMvcTest(controllers = DebtTransactionController.class)
@WithAnonymousUser
class DebtTransactionControllerTestAnonumous {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private DebtTransactionService debtTransactionService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	/*
	 * тестирую что анонимный пользователь может открыть страницу создания/редактирования DebtTransaction
	 */
	@Test
	void getGetDebtTransactionListPage_shold_be_ok_anonymous() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/debt-transaction/0").param("type", "INCREASE")).andExpect(view().name("debt-edit"));
		mockMvc.perform(get("/debt-transaction/0").param("type", "DECREASE")).andExpect(view().name("debt-edit"));
	}
	/*
	 * тестирую что анонимный пользователь может открыть страницу создания DebtTransaction
	 * ожидаю что страница будет открыта
	 */
	@Test
	void testGetDebTransactionEditPage_should_be_ok() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/debt-transaction/0").param("type", "INCREASE")).andExpect(view().name("debt-edit"));
		mockMvc.perform(get("/debt-transaction/0").param("type", "DECREASE")).andExpect(view().name("debt-edit"));
	}

	/*
	 * ожидаю что если запросить несуществующий DebtTransaction, получаю NOTFound страницу
	 */
	@Test
	void testGetDebtTransactionEditPahe_should_be_fail_not_found() throws Exception {
		when(debtTransactionService.findById(1)).thenReturn(null);
		mockMvc.perform(get("/debt-transaction/1")).andExpect(status().isNotFound());
	}
	
	/*
	 * ожидаю что если запросить на удаление не существующий DebtTransaction, получу NOT_FOUND страницу
	 */
	@Test
	void testDeleteDebtTransaction_should_be_fail_not_found() throws Exception {
		when(debtTransactionService.findById(1)).thenReturn(null);
		mockMvc.perform(get("/debt-transaction/delete/1")).andExpect(status().isNotFound());
	}
	
	/*
	 * ожидаю что если запросить существующий DebtTransaction  получу редирект
	 * на страницу список DebtTransaction /debt. 
	 * метод удаление будет вызван 1 раз
	 */
	@Test
	void testDeleteDebtTransaction_should_be_ok() throws Exception {
		when(debtTransactionService.findById(1)).thenReturn(new DebtTransaction());
		mockMvc.perform(get("/debt-transaction/delete/1")).andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/debt"));
		verify(debtTransactionService,times(1)).delete(any());
	}
	/*
	 * тестирую сохранение DebtTransaction, не заполнен partner, sum , endDate, ожадаю что будет возвращена страница редактирования
	 * с ошибоками
	 */
	@Test
	void testSaveDebtTransactionDecrease_create_new_should_be_fail_all_field_null() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		DebtTransaction testDebtTransaction = new DebtTransaction();
		mockMvc.perform(post("/debt-transaction/0").with(csrf()).flashAttr("debtTransaction", testDebtTransaction))
			.andExpect(view().name("debt-edit"))
			.andExpect(model().attributeHasFieldErrors("debtTransaction", "partner", "endDate", "sumTransaction"));
	}
	
	/*
	 * тестирую сохранение DebtTransaction, endDate в прошлом, ожадаю что будет возвращена страница редактирования
	 * с ошибоками
	 */
	@Test
	void testSaveDebtTransactionDecrease_create_new_should_be_fail_endDate_in_past() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		DebtTransaction testDebtTransaction = new DebtTransaction();
		testDebtTransaction.setPartner(new Partner());
		testDebtTransaction.setSumTransaction(BigDecimal.TEN);
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);
		cal.roll(10, false);
		testDebtTransaction.setEndDate(cal.getTime());
		mockMvc.perform(post("/debt-transaction/0").with(csrf()).flashAttr("debtTransaction", testDebtTransaction))
		.andExpect(view().name("debt-edit"))
		.andExpect(model().attributeHasFieldErrors("debtTransaction", "endDate"));
	}
	
	/*
	 * тестирую что если анонимный пользователь пытается сохранить траназкцию владельцем которой он не является
	 * получаю сообщение NOT FOUND
	 */
	@Test
	void testSaveDebtTransaction_should_be_fail_not_found() throws Exception {
		when(debtTransactionService.save(any())).thenThrow(DebtTransactionNotFoundExp.class);
		
		DebtTransaction testDebtTransaction = new DebtTransaction();
		testDebtTransaction.setPartner(new Partner());
		testDebtTransaction.setSumTransaction(BigDecimal.TEN);
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);
		cal.add(Calendar.DAY_OF_WEEK, 10);
		testDebtTransaction.setEndDate(cal.getTime());
		mockMvc.perform(post("/debt-transaction/0").with(csrf()).flashAttr("debtTransaction", testDebtTransaction))
			.andExpect(status().isNotFound());
	}
	/*
	 * тестирую что если все в порядке, пользователь может сохранить транзакцию
	 * 
	 */
	@Test
	void testSavetDebtTransaction_should_be_ok() throws Exception {
		DebtTransaction testDebtTransaction = new DebtTransaction();
		testDebtTransaction.setPartner(new Partner());
		testDebtTransaction.setSumTransaction(BigDecimal.TEN);
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);
		cal.add(Calendar.DAY_OF_WEEK, 10);
		testDebtTransaction.setEndDate(cal.getTime());
		mockMvc.perform(post("/debt-transaction/0").with(csrf()).flashAttr("debtTransaction", testDebtTransaction))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/debt"));
	}
}
