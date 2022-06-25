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
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.MockConfiguration;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.TransferTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@WebMvcTest(controllers = TransferTransactionController.class)
@Import(MockConfiguration.class)
@WithMockUser
class TransferTransactionControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private TransferTransactionService transferTransactionService;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	
	/*
	 * тестирую что пользователь может запросить страницу создания TransferTransaction
	 */
	@Test
	void test_getTransferTransactionNew_should_be_ok() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/transfer/0"))
			.andExpectAll(view().name("transfer-betwen-bank"));
	}

	
	/*
	 * тестрирую что пользователь может получиь существующую TransferTransaction 
	 */
	@Test
	void testGetTransferTransactionExist_should_be_ok() throws Exception {
		when(transferTransactionService.getTransferTransactionById(any()))
			.thenReturn(new TransferTransaction());
		
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		
		mockMvc.perform(get("/transfer/1"))
		.andExpectAll(view().name("transfer-betwen-bank"));
			
			
	}
	
	/*
	 * тестирую что если запрашивает не существующую TransferTransaction
	 * пользователь получает сообщение NotFound
	 */
	@Test
	void testGetTransaferTransactionExist_should_be_fail() throws Exception {
		when(transferTransactionService.getTransferTransactionById(any())).thenThrow(NoSuchElementException.class);
		
		mockMvc.perform(get("/transfer/1"))
		.andExpectAll(status().isNotFound());
	}
	
	/*
	 * тестирую что пользователь не может сохранить TransferTransaction, ошибка валидации 
	 */
	@Test
	void testSaveNewTransferTransaction_shoud_be_fail_sum_null() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		
		BankAccount sourceBankAccount = new BankAccount();
		sourceBankAccount.setName("source");
		
		BankAccount destinationBankAccount = new BankAccount();
		destinationBankAccount.setName("destination");
		
		TransferTransaction theTransferTransaction = new TransferTransaction();
		theTransferTransaction.setFromBankAccount(sourceBankAccount);
		theTransferTransaction.setToBankAccount(destinationBankAccount);
		
		mockMvc.perform(post("/transfer").with(csrf()).flashAttr("transferTransaction",theTransferTransaction))
			.andExpect(view().name("transfer-betwen-bank"))
			.andExpect(model().attributeHasFieldErrors("transferTransaction", "sumTransactionFrom"));
		
	}
	/*
	 * тестирую что пользователь не может сохранить TransferTransaction если указал одинаковые банки
	 */
	@Test
	void testSaveNewTransferTransaction_should_be_fail() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		
		BankAccount sourceBankAccount = new BankAccount();
		sourceBankAccount.setName("source");
		
		TransferTransaction theTransferTransaction = new TransferTransaction();
		theTransferTransaction.setFromBankAccount(sourceBankAccount);
		theTransferTransaction.setToBankAccount(sourceBankAccount);
		theTransferTransaction.setSumTransactionFrom(BigDecimal.TEN);
		
		mockMvc.perform(post("/transfer").with(csrf()).flashAttr("transferTransaction",theTransferTransaction))
			.andExpect(view().name("transfer-betwen-bank"))
			.andExpect(model().attributeExists("error"));
	}
	
	/*
	 * тестирую что если все в порядке пользователь может сохранить TransferTransaction
	 * ожидаю редирект на страницу список TransferTransaction
	 * ожидаю что вызов метода saveTransferTransaction был вызван как минимум 1 раз
	 */
	@Test
	void testSaveNewTransferTransaction_should_be_ok() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		
		BankAccount sourceBankAccount = new BankAccount();
		sourceBankAccount.setName("source");
		
		BankAccount destinationBankAccount = new BankAccount();
		destinationBankAccount.setName("destination");
		
		TransferTransaction theTransferTransaction = new TransferTransaction();
		theTransferTransaction.setFromBankAccount(sourceBankAccount);
		theTransferTransaction.setToBankAccount(destinationBankAccount);
		theTransferTransaction.setSumTransactionFrom(BigDecimal.TEN);
		mockMvc.perform(post("/transfer").with(csrf()).flashAttr("transferTransaction",theTransferTransaction))
			.andExpect(redirectedUrl("/transfer"));
		
		verify(transferTransactionService,times(1)).saveTransferTransaction(any());
	}
	
	/*
	 * тестирую что пользователь не может удалить Transfer Transaction так как такой не существует
	 */
	@Test
	void testDeleteTransferTransaction_should_be_fail() throws Exception {
		when(transferTransactionService.getTransferTransactionById(1L)).thenThrow(NoSuchElementException.class);
		mockMvc.perform(get("/transfer/delete/1")).andExpect(status().isNotFound());
	}
	
	/*
	 * тестирую что пользователь может удалить TransferTransaction
	 */
	@Test
	void testDeleteTransferTransaction_should_be_ok() throws Exception {
		when(transferTransactionService.getTransferTransactionById(1L)).thenReturn(new TransferTransaction());
		mockMvc.perform(get("/transfer/delete/1")).andExpect(redirectedUrl("/transfer"));
		verify(transferTransactionService,times(1)).deleteTransferTransaction(any());
	}
	
	
	
}
