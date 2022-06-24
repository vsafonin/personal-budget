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
	
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testGetTransactionShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		mockMvc.perform(get("/transaction/1").with(user(theTestUser)))
			.andExpect(status().isOk())
			.andExpect(view().name("transaction-adjustment-edit-page"));
	}
	
	@Test
	void testGetTransactionShouldBeFailBadUser() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		AppUser theBadUser = new AppUser("bad","user","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theBadUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		
		mockMvc.perform(get("/transaction/1").with(user(theBadUser)))
			.andExpect(status().isForbidden());
	}
	
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditDecreaseAdjustmentShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		mockMvc.perform(post("/transaction/1").with(csrf()).with(user(theTestUser))
				.flashAttr("transaction", newUsrTransaction)
				)
		//.andExpect(status().isOk())
		.andExpect(redirectedUrl("/bank-account/" + theTestBankAccount.getId()));
		
		verify(transactionService,times(1)).save(newUsrTransaction);
	}
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditIncreaseAdjustmentShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		mockMvc.perform(post("/transaction/1").with(csrf()).with(user(theTestUser))
				.flashAttr("transaction", newUsrTransaction)
				)
		//.andExpect(status().isOk())
		.andExpect(redirectedUrl("/bank-account/" + theTestBankAccount.getId()));
		
		verify(transactionService,times(1)).save(newUsrTransaction);
	}
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditShouldBeFailBadUser() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		AppUser badTestUser = new AppUser("bed","user","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(badTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		mockMvc.perform(post("/transaction/1").with(csrf()).with(user(theTestUser))
				.flashAttr("transaction", newUsrTransaction)
				)
		//.andExpect(status().isOk())
		.andExpect(status().is4xxClientError());
		
		verify(transactionService,times(0)).save(newUsrTransaction);
	}
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testPostTransactionEditShouldBeFailUsrTransactionNotFound() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		AdjustmentTransaction newUsrTransaction = (AdjustmentTransaction) usrTransaction.clone();
		newUsrTransaction.setSumTransaction(BigDecimal.valueOf(40));
		
		when(transactionService.getTransactioById(1L)).thenReturn(null);
		mockMvc.perform(post("/transaction/1").with(csrf()).with(user(theTestUser))
				.flashAttr("transaction", newUsrTransaction)
				)
		//.andExpect(status().isOk())
		.andExpect(status().is4xxClientError());
		
		verify(transactionService,times(0)).save(newUsrTransaction);
	}
	
	
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeleteUsrTransactionShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		mockMvc.perform(get("/transaction/delete/1").with(user(theTestUser))).andExpect(redirectedUrl("/bank-account/"  + theTestBankAccount.getId()));
		verify(transactionService,times(1)).delete(usrTransaction);
	}
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeleteUsrTransactionShouldBeFauilBadUser() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		AppUser badTestUser = new AppUser("bad","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
		usrTransaction.setAppUser(badTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(transactionService.getTransactioById(1L)).thenReturn(usrTransaction);
		
		mockMvc.perform(get("/transaction/delete/1").with(user(theTestUser))).andExpect(status().is4xxClientError());
		verify(transactionService,times(0)).delete(usrTransaction);
	}
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeleteUsrTransactionShouldBeFauilUsrTransactionNotFound() throws Exception {
		AppUser badTestUser = new AppUser("bad","pupa","123","123","pupa@mail.ru",true);

		
		when(transactionService.getTransactioById(1L)).thenReturn(null);
		
		mockMvc.perform(get("/transaction/delete/1").with(user(badTestUser))).andExpect(status().is4xxClientError());
		verify(transactionService,times(0)).delete(any());
	}

}
