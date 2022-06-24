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
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BankAccountController.class)
@Import(MockConfiguration.class)
class BankAccountControllerTestCreateNew {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BankAccountService accountService;

	@MockBean
	private AdjustmentTransactionService adjustmentTransactionService;
	
	@MockBean
	private UserService userService;
	
	
	@Mock
    private Authentication auth;

	@Test
	void testGetBankAccountCreatePageShouldRedirect302() throws Exception {
		mockMvc.perform(get("/bank-account/0")).andExpect(status().is3xxRedirection());

	}

	@Test
	@WithMockUser(username = "pupa", roles = "user", password = "123")
	void testGetBankAccountCreatePageShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		mockMvc.perform(get("/bank-account/0").with(user(theUser)).servletPath("/bank-account")).andExpect(status().isOk())
				.andExpect(view().name("bankaccount-edit-page")).andExpect(model().attributeExists("bankAccount"));
	}

	@Test
	@WithMockUser(username = "pupa", roles = "user", password = "123")
	void testSaveNewBankAccountShouldBeOk() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setAppUser(theUser);
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		mockMvc.perform(
				post("/bank-account/0").flashAttr("bankAccount", theBankAccount).with(csrf()))
				.andExpect(redirectedUrl("/"));

		verify(accountService, times(1)).save(theBankAccount);

	}

	@Disabled // cause now we always check user, and anonymous user isn't allowed
	@Test
	@WithMockUser(username = "pupa", roles = "user", password = "123")
	void testSaveNewBankAccountShouldBeFailAppUserNull() throws Exception {
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		mockMvc.perform(post("/bank-account/0").flashAttr("bankAccount", theBankAccount).with(csrf()))
				.andExpect(view().name("bankaccount-edit-page"))
				.andExpect(model().attributeHasFieldErrors("bankAccount", "appUser"));

		verify(accountService, times(0)).save(theBankAccount);

	}

	@Test
	@WithMockUser(username = "pupa", roles = "user", password = "123")
	void testSaveNewBankAccountShouldBeFailNameLessThen2() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("2");
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setAppUser(theUser);
		when(auth.getPrincipal()).thenReturn(theUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		
		mockMvc.perform(
				post("/bank-account/0").flashAttr("bankAccount", theBankAccount).with(csrf()).with(user(theUser))
				.servletPath("/bank-account"))
				.andExpect(view().name("bankaccount-edit-page"))
				.andExpect(model().attributeHasFieldErrors("bankAccount", "name"));
		
		verify(accountService, times(0)).save(theBankAccount);

	}

	@Test
	@WithMockUser(username = "pupa", roles = "user", password = "123")
	void testSaveNewBankAccountShouldBeFailBalanceIsNull() throws Exception {
		AppUser theUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theUser);
		theBankAccount.setAppUser(theUser);
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setBalance(null);
		mockMvc.perform(post("/bank-account/0").flashAttr("bankAccount", theBankAccount).with(csrf()).servletPath("/bank-account")
				.with(user(theUser))).andExpect(view().name("bankaccount-edit-page"))
				.andExpect(model().attributeHasFieldErrors("bankAccount", "balance"));

		verify(accountService, times(0)).save(theBankAccount);

	}

}
