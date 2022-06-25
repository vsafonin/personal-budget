package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SalaryTransactionController.class)
@WithMockUser
class SalaryTransactionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BankAccountService bankAccountService;

	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private SalaryTransactionService salaryTransactionService;
	
	@MockBean 
	private PartnerRepository pathnerRepository;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@MockBean
	private UserService userService;
	
	
	/*
	 * тестирую что пользователь может запросить страницу создания новой SalaryTransaction
	 */
	@Test
	void testGetSalaryTransactionPageGetNew() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/salary-transaction/0"))
			.andExpect(view().name("transaction-salary"));
	}
	/*
	 * тестирую что если запросить не существующую SalaryTransaction будет выдана ошибка NOT FOUND
	 */
	@Test
	void testGetSalaryTransactionPageGetNewShouldBeFailBadId() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(salaryTransactionService.findById(10)).thenThrow(NoSuchElementException.class);
		
		mockMvc.perform(get("/salary-transaction/10").with(user(theTestUser)))
		.andExpect(status().isNotFound());
	}
	
	/*
	 * тестирую что если неверно заполнена SalaryTransaction получаю страницу редактирования
	 */
	@Test
	void testSaveSalaryTransaction_should_be_fail_valid_field() throws Exception {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(post("/salary-transaction/0").with(csrf()).flashAttr("salaryTransaction", new SalaryTransaction()))
			.andExpect(view().name("transaction-salary"))
			.andExpect(model().attributeHasFieldErrors("salaryTransaction", "sumTransaction","partner"));
	}
	
	/*
	 * тестирую что если транзакция верно заполнена ее можно сохранить
	 */
	@Test
	void testSaveSalaryTransactionPage() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.valueOf(10), "test bank", theTestUser, Currency.getInstance("RUB"));
		Partner thePartner = new Partner();
		thePartner.setAppUser(theTestUser);
		thePartner.setName("lupa");
		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		theSalaryTransaction.setAppUser(theTestUser);
		theSalaryTransaction.setBankAccount(theTestBankAccount);
		theSalaryTransaction.setDescription("test salary");
		theSalaryTransaction.setPartner(thePartner);
		theSalaryTransaction.setSumTransaction(BigDecimal.TEN);
		
		mockMvc.perform(post("/salary-transaction/0")
				.with(csrf())
				.flashAttr("salaryTransaction", theSalaryTransaction))
		.andExpect(redirectedUrl("/salary"));
		
		verify(salaryTransactionService,times(1)).save(theSalaryTransaction);
	}
	
	/*
	 * тестирую что при запросе удаления существующей транзакции происходит удаление
	 */
	@Test
	void testDeleteSalaryTransaction() throws Exception {
		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		when(salaryTransactionService.findById(10)).thenReturn(theSalaryTransaction);
		mockMvc.perform(get("/salary-transaction/delete/10")).andExpect(redirectedUrl("/salary"));
		verify(salaryTransactionService,times(1)).delete(theSalaryTransaction);
	}
	/*
	 * тестирую что при запросе удаление НЕ существующей транзакции удаления не происход, пользователь получает 
	 * NOT FOUND
	 */
	@Test
	void testDeleteSalaryTransactionShouldBeFail() throws Exception {
		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		when(salaryTransactionService.findById(10)).thenThrow(NoSuchElementException.class);
		mockMvc.perform(get("/salary-transaction/delete/10")).andExpect(status().isNotFound());
		verify(salaryTransactionService,times(0)).delete(theSalaryTransaction);
	}

}
