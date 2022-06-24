package ru.vladimir.personalaccounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtPayment;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.BankAccountRepository;
import ru.vladimir.personalaccounter.repository.DebtPaymentRepository;
import ru.vladimir.personalaccounter.repository.DebtTransactionRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.repository.UserRepository;
@SpringBootTest
class DebtPaymentServiceImplTestIT {

	@Autowired
	private DebtPaymentService debtPaymentService;
	
	@Autowired
	private DebtTransactionService debtTransactionService;
	
	@Autowired
	private BankAccountService accountService;
	
	@Autowired 
	private UserService userService;
	
	@Autowired
	private DebtPaymentRepository debtPaymentRepository;
	@Autowired
	private DebtTransactionRepository debtTransactionRepository;
	@Autowired
	private PartnerRepository partnerRepository;
	@Autowired
	private BankAccountRepository bankAccountRepository;
	@Autowired
	private UserRepository userRepository;
	
	@Mock
	private Authentication auth;

	private AppUser theTestUser;

	@BeforeEach
	public void initSecurityContext() {
		theTestUser = new AppUser("pupadebt", "pupadebt", "123", "123", "pupadebt@mail.ru", true);
		when(auth.getPrincipal()).thenReturn(theTestUser);
		SecurityContextHolder.getContext().setAuthentication(auth);
		when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("pupadebt");

	}
	
	@AfterEach
	private void delete() {
		debtPaymentRepository.findAll().stream().forEach(cp -> debtPaymentRepository.delete(cp));
		debtTransactionRepository.findAll().stream().forEach(cp -> debtTransactionRepository.delete(cp));
		partnerRepository.findAll().stream().forEach(cp -> partnerRepository.delete(cp));
		bankAccountRepository.findAll().stream().forEach(cp -> bankAccountRepository.delete(cp));
		userRepository.findAll().stream().forEach(cp -> userRepository.delete(cp));
	}
	
	@Test
	void testSave() {
		//create new debtTransaction (ct)
		DebtTransaction dt = new DebtTransaction();
		Partner p = new Partner();
		BankAccount theBankAccount = new BankAccount();
		
		userService.save(theTestUser);
		theBankAccount.setAppUser(theTestUser);
		theBankAccount.setCurrency(Currency.getInstance("RUB"));
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		accountService.save(theBankAccount);
		dt.setBankAccount(theBankAccount);
		dt.setAppUser(theTestUser);
		dt.setPartner(p);
		dt.setCreateTime(new Timestamp(System.currentTimeMillis()));
		dt.setTypeOfOperation(TypeOfOperation.DECREASE);
		dt.setSumTransaction(BigDecimal.valueOf(100));
		dt.setDebtPayment(new ArrayList<DebtPayment>());
		//save ct in db
		long idct = debtTransactionService.save(dt).getId();
		//create new debt payment (cp)
		DebtPayment dp = new DebtPayment();
		//set ct in ct
		dp.setDebtTransaction(dt);
		dp.setPaySum(BigDecimal.TEN);
		
		
		//save in db
		debtPaymentService.save(dp);
		//ceck sum ct (it must be less)
		DebtTransaction ctInDb = debtTransactionService.findById(idct);
		if (ctInDb == null) {
			fail();
		}
		assertThat(ctInDb.getSumTransaction().compareTo(BigDecimal.valueOf(90))).isEqualTo(0);
	}

	@Test
	void testDelete() {
		//create new debtTransaction (dt)
				DebtTransaction dt = new DebtTransaction();
				Partner p = new Partner();
				BankAccount theBankAccount = new BankAccount();
				theBankAccount.setName("test");
				userService.save(theTestUser);
				theBankAccount.setAppUser(theTestUser);
				theBankAccount.setCurrency(Currency.getInstance("RUB"));
				theBankAccount.setBalance(BigDecimal.valueOf(100));
				accountService.save(theBankAccount);
				dt.setBankAccount(theBankAccount);
				dt.setAppUser(theTestUser);
				dt.setPartner(p);
				dt.setCreateTime(new Timestamp(System.currentTimeMillis()));
				dt.setTypeOfOperation(TypeOfOperation.DECREASE);
				dt.setSumTransaction(BigDecimal.valueOf(100));
				dt.setDebtPayment(new ArrayList<DebtPayment>());
				//save ct in db
				long iddt = debtTransactionService.save(dt).getId();
				//create new debt payment (dp)
				DebtPayment dp = new DebtPayment();
				//set ct in ct
				dp.setDebtTransaction(dt);
				dp.setPaySum(BigDecimal.TEN);
				//save in db
				debtPaymentService.save(dp);
				debtPaymentService.delete(dp);
				//ceck sum ct (it must be less)
				DebtTransaction dtInDb = debtTransactionService.findById(iddt);
				if (dtInDb == null) {
					fail();
				}
				
				
				assertThat(dtInDb.getSumTransaction().compareTo(BigDecimal.valueOf(100))).isEqualTo(0);
	}

}
