package ru.vladimir.personalaccounter.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtPayment;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtPaymentService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DebtPaymentController.class)
class DebtPaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private DebtTransactionService debtTransactionService;
	
	@MockBean
	private DebtPaymentService debtPaymentService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@MockBean
	private UserService userService;
	/*
	 * тестирую что анонимный пользователь может открыть страницу с DebtTransactions
	 */
	@Test
	@WithAnonymousUser
	void testDebtPayment_get_edit_page_should_be_ok_anounymous() throws Exception {
		long debtTransactionId = 1L;
		long debtPaymentId = 1L;
		when(debtPaymentService.findById(debtPaymentId)).thenReturn(new DebtPayment());
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/debt-transaction/payment/"+debtPaymentId +"?dtid="+debtTransactionId))
			.andExpect(view().name("debt-payment"));
	}
	
	/*
	 * тестирую что авторизованный пользователь может открыть страницу редактировная DebtPayement
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDebtPayment_get_edit_page_should_be_ok()throws Exception{
		long debtTransactionId = 1L;
		long debtPaymentId = 1L;
		when(debtPaymentService.findById(debtPaymentId)).thenReturn(new DebtPayment());
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(new AppUser());
		mockMvc.perform(get("/debt-transaction/payment/"+debtPaymentId +"?dtid="+debtTransactionId))
			.andExpect(view().name("debt-payment"));
	}
	
	/*
	 * тестирую что авторизованный пользователь может сохранить DebtPayment
	 * 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testSavedebtPayment() throws Exception {
		//create user
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		//create bank account
		BankAccount theTestBankAccount = new BankAccount("test", 
				BigDecimal.valueOf(100), "test bank", theTestUser, Currency.getInstance("RUB"));
		//create partner
		Partner thePartner = new Partner();
		thePartner.setAppUser(theTestUser);
		thePartner.setName("lupa");
		//create debt transaction
		DebtTransaction thedebtTransaction = new DebtTransaction();
			thedebtTransaction.setId(2L);
			thedebtTransaction.setActive(true);
			thedebtTransaction.setAppUser(theTestUser);
			thedebtTransaction.setBankAccount(theTestBankAccount);
			thedebtTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
			thedebtTransaction.setCurrency(theTestBankAccount.getCurrency());
			thedebtTransaction.setEndDate(new Timestamp(System.currentTimeMillis() + 86400000));
			thedebtTransaction.setPartner(thePartner);
			thedebtTransaction.setSumTransaction(BigDecimal.valueOf(100));
			thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		//create debt payment
		DebtPayment thedebtPayment = new DebtPayment();
			thedebtPayment.setDebtTransaction(thedebtTransaction);
			thedebtPayment.setPayDate(new Timestamp(System.currentTimeMillis()));
			thedebtPayment.setPaySum(BigDecimal.valueOf(5));
		
		//when
		when(debtTransactionService.findById(2L)).thenReturn(thedebtTransaction);
		//send mock 
		mockMvc.perform(post("/debt-transaction/payment/0").with(csrf()).flashAttr("debtPayment", thedebtPayment))
			//test redirect
			.andExpect(redirectedUrl("/debt-transaction/2?type=DECREASE"));
	}
	/*
	 * тестирую что если пользователь запрашивает удаление DebtPayment которая не существует или у него нет туда доступа
	 * его перебрасывает на страницу 404
	 */
	@Test
	@WithMockUser(roles = "user",username = "pupa")
	void testDeletedebtPaymentShouldBeFailUser() throws Exception {
		when(debtPaymentService.findById(1L)).thenReturn(null);
		mockMvc.perform(get("/debt-transaction/payment/delete/1"))
				.andExpect(status().isNotFound());
	}
	/*
	 * тестирую что если пользователь авторизован и у него есть право удалять DebtPayment
	 * он может это сделать. 
	 * ожидаю что перебросить на странцу DebtTransaction которая является владельцем DebtPayment.
	 * 
	 */
	@Test
	@WithMockUser(roles = "user", username = "pupa")
	void testDeletedebtPaymentShouldBeOk() throws Exception {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theTestUser);
		DebtTransaction thedebtTransaction = new DebtTransaction();
		thedebtTransaction.setId(1L);
		thedebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		DebtPayment thedebtPayment = new DebtPayment();
		thedebtPayment.setDebtTransaction(thedebtTransaction);
		when(debtPaymentService.findById(1L)).thenReturn(thedebtPayment);
		mockMvc.perform(get("/debt-transaction/payment/delete/1"))
			.andExpect(redirectedUrl("/debt-transaction/1?type=DECREASE"));
	}

}
