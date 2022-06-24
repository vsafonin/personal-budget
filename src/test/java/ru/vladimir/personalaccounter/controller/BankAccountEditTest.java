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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
class BankAccountEditTest {

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
	
	@Mock
    private Authentication auth;
	
	@BeforeEach
	void setUp() {
		AppUser theAppUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(auth.getPrincipal()).thenReturn(theAppUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	void testGetEditBankAccountShouldBeFailAnounimousUser() throws Exception {
		mockMvc.perform(get("/bank-account/1")).andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	void testGetEditBankAccountShouldBeFailIllegalUser() throws Exception {
//		AppUser theAppUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		AppUser theFakeAppUser = new AppUser("fake", "fake", "123", "123", "fake@mail.ru", true);
//		BankAccount theBankAccount = new BankAccount("test", 100, "test", theAppUser, Currency.getInstance("RUB"));
		when(bankAccountService.getBankAccountById(1L)).thenThrow(new BankAccountNotFoundException(""));
		mockMvc.perform(get("/bank-account/1").with(user(theFakeAppUser)).servletPath("/bank-account")).andExpect(status().is4xxClientError());
	}

	@Test
	void testGetEditBankAccountShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		theBankAccount.setId(1L);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		mockMvc.perform(get("/bank-account/1").with(user(theUser)).servletPath("/bank-account")).andExpect(status().isOk())
				.andExpect(view().name("bankaccount-edit-page"));
	}

	@Test
	void testGetDeleteBankAccountShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount("test", BigDecimal.valueOf(100), "test", theUser, Currency.getInstance("RUB"));
		theBankAccount.setId(1L);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		when(bankAccountService.getBankAccountById(1L)).thenReturn(theBankAccount);
		mockMvc.perform(get("/bank-account/delete/1").with(user(theUser))).andExpect(redirectedUrl("/"));
		verify(bankAccountService, times(1)).delete(theBankAccount);
	}

	// test post method
	@Test
	@WithMockUser(roles = "user")
	void testEditBankAccountPostShouldBeFailValidCurrencyIsNull() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount();
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		theBankAccount.setName("test");
		theBankAccount.setAppUser(theUser);
		mockMvc.perform(post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theUser))
				).andExpect(model().hasErrors())
				.andExpect(model().attributeHasFieldErrors("bankAccount", "currency"));
	}

	@Test
	@WithMockUser(roles = "user", username = "pupa", password = "$2a$10$sq.M51v8.gbvWlkx6tPXsuwROmqCPLay20G9v5.C/RxzxdGdS311K")
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

	// i move checj null bank account to service method
	@Disabled
	@Test
	@WithMockUser(roles = "user", username = "pupa", password = "$2a$10$sq.M51v8.gbvWlkx6tPXsuwROmqCPLay20G9v5.C/RxzxdGdS311K")
	void testEditBankAccountPostShouldBeFailBankAccountNotFoundExceptio() throws Exception {
		AppUser theAppUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setAppUser(theAppUser);
		when(bankAccountService.getBankAccountById(any())).thenReturn(null);
		mockMvc.perform(
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theAppUser)))
				.andExpect(status().is4xxClientError());
		verify(bankAccountService, times(0)).save(theBankAccount);

	}

	@Test
	@WithMockUser(roles = "user", username = "pupa", password = "$2a$10$sq.M51v8.gbvWlkx6tPXsuwROmqCPLay20G9v5.C/RxzxdGdS311K")
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
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theUser)))
				.andExpect(model().hasNoErrors()).andExpect(redirectedUrl("/bank-account/1?success=true"));

		verify(bankAccountService, times(1)).save(theBankAccount);
		assertFalse(theBankAccount.getTransactions() == null);
		assertFalse(theBankAccount.getTransactions().stream().findFirst().orElse(null)
				.getTypeOfOperation() == TypeOfOperation.INCREASE);

	}

	@Test
	@WithMockUser(roles = "user", username = "pupa", password = "$2a$10$sq.M51v8.gbvWlkx6tPXsuwROmqCPLay20G9v5.C/RxzxdGdS311K")
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
				post("/bank-account/1").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theUser)))
				.andExpect(model().hasNoErrors()).andExpect(redirectedUrl("/bank-account/1?success=true"));

		verify(bankAccountService, times(1)).save(theBankAccount);
		assertFalse(theBankAccount.getTransactions() == null);
		assertFalse(theBankAccount.getTransactions().stream().findFirst().orElse(null)
				.getTypeOfOperation() == TypeOfOperation.DECREASE);

	}

}
