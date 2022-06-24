package ru.vladimir.personalaccounter.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
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

import org.junit.jupiter.api.Disabled;
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
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.BankAccountNotFoundException;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BankAccountController.class)
@Import(MockConfiguration.class)
class BankAccountControllerEditOperationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private BankAccountService bankAccountService;

	@MockBean
	private AdjustmentTransactionService adjustmentTransactionService;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	/*
	 * тестирую что неавторизованный пользователь получить страницу банк аккаунта не может,
	 * и его кидает на страницу авторизации.
	 */
	@Test
	void testGetEditBankAccountShouldBeFailAnounimousUser() throws Exception {
		mockMvc.perform(get("/bank-account/1")).andExpect(redirectedUrl("http://localhost/login"));
	}
	
	/*
	 * тестирую что если пользователь пытается получить доступ к BankAccount который ему не принадлежит
	 * получает сообщение NOT FOUND
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetEditBankAccountShouldBeFailIllegalUser() throws Exception {
		when(bankAccountService.getBankAccountById(1L)).thenThrow(BankAccountNotFoundException.class);
		mockMvc.perform(get("/bank-account/1"))
			.andExpect(status().isNotFound());
	}
	/*
	 * тестирую что если пользователь авторизован и он владелец BankAccount он получает страницу редактирования
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetEditBankAccountShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		theBankAccount.setId(1L);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		
		mockMvc.perform(get("/bank-account/1")).andExpect(status().isOk())
				.andExpect(view().name("bankaccount-edit-page"));
	}
	
	/*
	 *тестирую что если пользователь авторизован и он владелец BankAccount и у BankAccount нет транзакций
	 *он может удалить этот счет. Проверяем что метод удаления был вызван 1 раз.
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetDeleteBankAccountShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		theBankAccount.setId(1L);
		when(bankAccountService.bankAccountHasTransaction(theBankAccount)).thenReturn(false);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		
		mockMvc.perform(get("/bank-account/delete/1"))
			.andExpect(redirectedUrl("/"));
		
		verify(bankAccountService, times(1)).delete(theBankAccount);
	}
	
	/*
	 * Тестирую что если у BankAccount есть транзакции, будет выброшена страница редактирования и на ней
	 * будет готовая ссылка с параметром удаления всех транзакций.Метод удаления не был вызван ниразу
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testEditBankAccountShouldReturnPageWithForceParameterDeleteTransaction() throws Exception{
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		theBankAccount.setId(1L);
		when(bankAccountService.bankAccountHasTransaction(theBankAccount)).thenReturn(true);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		
		mockMvc.perform(get("/bank-account/delete/1"))
		.andExpect(view().name("bankaccount-edit-page"))
		.andExpect(model().attributeExists("bankAccountHasTransaction"));
	
		verify(bankAccountService, times(0)).delete(theBankAccount);
		
	}
	
	/*
	 * Тестирую что если пользователь хочет удалить BankAccount и у него есть транзакции, но пользователь указал параметр force
	 * происходит удаление bankAccount.
	 * Пользователя редиректит на /
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetDeleteBankAccountShouldBeOkBankAccountHasTransactions() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		theBankAccount.setId(1L);
		when(bankAccountService.bankAccountHasTransaction(theBankAccount)).thenReturn(true);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		
		mockMvc.perform(get("/bank-account/delete/1?force=true"))
			.andExpect(redirectedUrl("/"));
		
		verify(bankAccountService, times(1)).delete(theBankAccount,true);
	}

	/*
	 * хотя это не возможно, но все же тестирую что пользователь не убрал Curreny со страницы редактирования
	 * ожидаю что будет возвращена страница редактирования.
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testEditBankAccountPostShouldBeFailValidCurrencyIsNull() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setAppUser(theUser);
		
		mockMvc.perform(post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf())
				).andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("bankAccount", "currency"))
				.andExpect(view().name("bankaccount-edit-page"));
	}
	/*
	 * тестирую что пользователь может сохранить изменения в BankAccount, никаких ошибок быть не должно.
	 * ожидаю что его перебросить на страницу Редактирование BankAccount с параметром success. 
	 * будет вызван метод сохранения BankAccount 1 раз. 
	 * 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testEditBankAccountPostShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setAppUser(theUser);
		when(bankAccountService.getBankAccountById(any())).thenReturn(theBankAccount);
		mockMvc.perform(
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theUser)))
				.andExpect(model().hasNoErrors()).andExpect(redirectedUrl("/bank-account/1?success=true"));

		verify(bankAccountService, times(1)).save(theBankAccount);

	}

	/*
	 * тестирую что пользователь может сохранить BankAccount, сумму он поменял. 
	 * ожидаю что будет создана транзакция AdjustmentTransaction. 
	 * пользователя перебросить на страницу редактирования BankAccount с параметром success.
	 * у theBankAccount будет не пустой список AdjustmentTransactions
	 * будет вызван метод сохранения BankAccount 1 раз. 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testEditBankAccountPostShouldBeCreateBankAccountAdjustmentDecrease() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setAppUser(theUser);
		BankAccount oldBankAccount = new BankAccount(theBankAccount);
		theBankAccount.setBalance(BigDecimal.valueOf(50));
		when(bankAccountService.getBankAccountById(any())).thenReturn(oldBankAccount);

		mockMvc.perform(
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()))
				.andExpect(model().hasNoErrors()).andExpect(redirectedUrl("/bank-account/1?success=true"));

		
		assertFalse(theBankAccount.getTransactions() == null);
		assertFalse(theBankAccount.getTransactions().stream().findFirst().orElse(null)
				.getTypeOfOperation() == TypeOfOperation.INCREASE);
		
		verify(bankAccountService, times(1)).save(theBankAccount);
	}

	/*
	 * тестирую что пользователь может сохранить BankAccount, сумму он поменял. 
	 * ожидаю что будет создана транзакция AdjustmentTransaction cтипом DECREASE. 
	 * пользователя перебросит на страницу редактирования BankAccount с параметром success.
	 * у theBankAccount будет не пустой список AdjustmentTransactions
	 * будет вызван метод сохранения BankAccount 1 раз. 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testEditBankAccountPostShouldBeCreateBankAccountAdjustmentIncrease() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setAppUser(theUser);
		BankAccount oldBankAccount = new BankAccount(theBankAccount);
		oldBankAccount.setBalance(BigDecimal.valueOf(50));
		when(bankAccountService.getBankAccountById(any())).thenReturn(oldBankAccount);

		mockMvc.perform(
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()))
				.andExpect(model().hasNoErrors()).andExpect(redirectedUrl("/bank-account/1?success=true"));

		
		assertFalse(theBankAccount.getTransactions() == null);
		assertFalse(theBankAccount.getTransactions().stream().findFirst().orElse(null)
				.getTypeOfOperation() == TypeOfOperation.DECREASE);
		
		verify(bankAccountService, times(1)).save(theBankAccount);
	}

}
