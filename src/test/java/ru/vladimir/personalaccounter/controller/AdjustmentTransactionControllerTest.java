package ru.vladimir.personalaccounter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AdjustmentTransactionController.class)
@Import(MockConfiguration.class)
class AdjustmentTransactionControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private AdjustmentTransactionService transactionService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	
	@MockBean
	private UserService userService;
	
	@BeforeEach
	private void setUp() {
		AppUser theUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);

	}
	/*
	 * Тестирую что можно запросить страницу с транзакциями корректировок
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetTransactionShouldBeOk() throws Exception {

		when(transactionService.getTransactioById(1L)).thenReturn(new AdjustmentTransaction());
		
		mockMvc.perform(get("/adjustment-transaction/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("transaction-adjustment-edit-page"));
	}
	
	/*
	 * тестирую что если пользователь запросит транзакцию корректировок,
	 * которой  нет получит сообщение NOT FOUND
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetTransactionShouldBeFailBadUser() throws Exception {
		
		when(transactionService.getTransactioById(1L)).thenReturn(null);
		
		mockMvc.perform(get("/adjustment-transaction/1"))
			.andExpect(status().isNotFound());
	}

	/*
	 * тестирую что можно сохранить изменения транзакции c типом Decrease
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditDecreaseAdjustmentShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		mockMvc.perform(post("/adjustment-transaction/1").with(csrf())
				.flashAttr("transaction", newUsrTransaction)
				)
		//.andExpect(status().isOk())
		.andExpect(redirectedUrl("/bank-account/" + theTestBankAccount.getId()));
		
		verify(transactionService,times(1)).save(newUsrTransaction);
	}
	
	/*
	 * тестирую что можно сохранить изменения транзакции с типом Increse
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditIncreaseAdjustmentShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		mockMvc.perform(post("/adjustment-transaction/1").with(csrf())
				.flashAttr("transaction", newUsrTransaction)
				)
		.andExpect(redirectedUrl("/bank-account/" + theTestBankAccount.getId()));
		
		verify(transactionService,times(1)).save(newUsrTransaction);
	}
	
	/*
	 * тестирую что при попытке сохранить транзакции которой не существует или другой владелец
	 * получу сообщение NOT FOUND
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditShouldBeFailBadUser() throws Exception {
	
		when(transactionService.getTransactioById(1L)).thenReturn(null);
		mockMvc.perform(post("/adjustment-ransaction/1").with(csrf())
				.flashAttr("transaction", new AdjustmentTransaction())
				)
				.andExpect(status().is4xxClientError());
		
		verify(transactionService,times(0)).save(any());
	}
	
	/*
	 *тестирую что при удалении меня переводит на страницу банковского счета владельца
	 *и один раз был вызван метод удаления
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeleteUsrTransactionShouldBeOk() throws Exception {
		
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		mockMvc.perform(get("/adjustment-transaction/delete/1").with(user(theTestUser)))
			.andExpect(redirectedUrl("/bank-account/"  + theTestBankAccount.getId()));
		
		verify(transactionService,times(1)).delete(usrTransaction);
	}
	/*
	 * тестирую что попытке удаление несуществующей или которой не является владельцем 
	 * получает сообщение NOT FOUND
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeleteUsrTransactionShouldBeFauilBadUser() throws Exception {
		
		when(transactionService.getTransactioById(1L)).thenReturn(null);
		
		mockMvc.perform(get("/transaction/delete/1")).andExpect(status().isNotFound());
		
		verify(transactionService,times(0)).delete(any());
	}

}
