package ru.vladimir.personalaccounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.repository.PartnerRepository;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
class SalaryTransactionServiceImplIntegrationTest {

	@Autowired
	private SalaryTransactionService salaryTransactionService;

	@Autowired
	private UserService userService;

	@Autowired
	private BankAccountService bankAccountService;

	@Autowired
	private PartnerRepository pathnerRepository;

	@Mock
	private Authentication auth;

	private AppUser theTestUser;

	@BeforeEach
	public void initSecurityContext() {
		theTestUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		when(auth.getPrincipal()).thenReturn(theTestUser);
		when(auth.getName()).thenReturn("pupa");
		SecurityContextHolder.getContext().setAuthentication(auth);
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(theTestUser);

	}

	@AfterEach
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@WithMockUser(password = "123", roles = "user", username = "pupa")
	void testSaveShouldBeOk() {
		userService.save(theTestUser);
		
		

		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser,
				Currency.getInstance("RUB"));
		bankAccountService.save(theTestBankAccount);

		Partner thePartner = new Partner();
		thePartner.setAppUser(theTestUser);
		thePartner.setName("test eployer");
		pathnerRepository.save(thePartner);

		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		theSalaryTransaction.setAppUser(theTestUser);
		theSalaryTransaction.setBankAccount(theTestBankAccount);
		theSalaryTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		theSalaryTransaction.setCurrency(theTestBankAccount.getCurrency());
		String line = "test salary with unique data \\\"G]wv!-JM5p<:!yc$/G:RxZJ\\\"R]\\\"%a,K<,![G#>*}+rp2Uq'%Ps'Kea*2\\\\JXCWX";
		theSalaryTransaction.setDescription(line);
		theSalaryTransaction.setPartner(thePartner);
		theSalaryTransaction.setSumTransaction(BigDecimal.valueOf(100));

		salaryTransactionService.save(theSalaryTransaction);

		// check in db
		List<SalaryTransaction> listOfSalary = salaryTransactionService.getAllSalaryTransactions();
		boolean result = false;
		for (SalaryTransaction transaction : listOfSalary) {
			if (transaction.getDescription().equals(line))
				result = true;
		}

		assertThat(result).isTrue();
		salaryTransactionService.delete(theSalaryTransaction);
		pathnerRepository.delete(thePartner);
		bankAccountService.delete(theTestBankAccount);
		userService.deleteUser(theTestUser);

	}

	@Test
	void testDelete() {
		userService.save(theTestUser);

		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser,
				Currency.getInstance("RUB"));
		bankAccountService.save(theTestBankAccount);

		Partner thePartner = new Partner();
		thePartner.setAppUser(theTestUser);
		thePartner.setName("test eployer");
		pathnerRepository.save(thePartner);

		SalaryTransaction theSalaryTransaction = new SalaryTransaction();
		theSalaryTransaction.setAppUser(theTestUser);
		theSalaryTransaction.setBankAccount(theTestBankAccount);
		theSalaryTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		theSalaryTransaction.setCurrency(theTestBankAccount.getCurrency());
		String line = "test salary with unique data \\\"G]wv!-JM5p<:!yc$/G:RxZJ\\\"R]\\\"%a,K<,![G#>*}+rp2Uq'%Ps'Kea*2\\\\JXCWX";
		theSalaryTransaction.setDescription(line);
		theSalaryTransaction.setPartner(thePartner);
		theSalaryTransaction.setSumTransaction(BigDecimal.valueOf(100));

		salaryTransactionService.save(theSalaryTransaction);

		//
		salaryTransactionService.delete(theSalaryTransaction);
		assertThat(theTestBankAccount.getBalance().compareTo(BigDecimal.TEN)).isEqualTo(0);
	}

}
