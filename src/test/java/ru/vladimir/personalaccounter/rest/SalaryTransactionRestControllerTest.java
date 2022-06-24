package ru.vladimir.personalaccounter.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Authorities;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.enums.AauthoritiesEnum;
import ru.vladimir.personalaccounter.methods.JwtProvider;
import ru.vladimir.personalaccounter.repository.AppUserJwtRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SalaryTransactionRestController.class)
class SalaryTransactionRestControllerTest {

	private final static String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjYyNDk0NDAwfQ.eNso11oEG-bwgIfV81cQH78QzvSp1bdhvXDCOSq9RHklWAkQulQ3-dhBc5Bathd8KLxELW2KurD1CqWYwkWXSA";
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private AppUserJwtRepository appUserJwtRepository;
	
	@MockBean
	private JwtProvider jwtProvider;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private SalaryTransactionService salaryTransactionService;
	
	@MockBean
	private PartnerRepository partnerRepository;
	
	@Mock
    private Authentication auth;
	
	@Autowired
	private ObjectMapper mapper;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private AdjustmentTransactionService adjustmentTransactionService;
	
	@MockBean
	private DebtTransactionService debtTransactionService;
	
	private AppUser theAppUser;
	@BeforeEach
	void setUp() {
		theAppUser = new AppUser("test", "test", "123", "123", "pupa@mail.ru", true);
		theAppUser.setRoles(Set.of(new Authorities(AauthoritiesEnum.USER.getRoleName())));
		when(auth.getPrincipal()).thenReturn(theAppUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theAppUser);
	}
	
	
	@Test
	void testGet10TransactionShouldBeFailAuth() throws Exception {
		mockMvc.perform(get("/api/income").header("authorization", "Bearer " + "ss")).andExpect(status().isUnauthorized());
		
	}
	
	@Test
	void testGet10TransactionShouldBeOk() throws Exception{
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		List<SalaryTransaction> transactions = new ArrayList<SalaryTransaction>();
		transactions.add(new SalaryTransaction());
		when(salaryTransactionService.getAllSalaryTransactions()).thenReturn(new ArrayList<SalaryTransaction>());
		mockMvc.perform(get("/api/income").header("authorization", "Bearer " + TOKEN))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
	}
	@Test
	void testPost10TransactionShouldBeOk() throws Exception{
		when(jwtProvider.validateToken(TOKEN)).thenReturn(true);
		when(jwtProvider.getLoginFromToken(TOKEN)).thenReturn("test");
		when(userDetailsService.loadUserByUsername("test")).thenReturn(theAppUser);
		
		//create salary transaction
		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		
		//create bank
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setName("test");
		when(bankAccountService.getBankAccounts()).thenReturn(List.of(theBankAccount));
		//create partner
		Partner thePartner = new Partner();
		thePartner.setName("test");
		
		theSalaryTransaction.setPartner(thePartner);
		theSalaryTransaction.setBankAccount(theBankAccount);
		theSalaryTransaction.setSumTransaction(BigDecimal.TEN);
		
		when(salaryTransactionService.save(any(SalaryTransaction.class))).thenReturn(theSalaryTransaction);
		
		mockMvc.perform(post("/api/salary").header("authorization", "Bearer " + TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(theSalaryTransaction))
				)
			.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

}
