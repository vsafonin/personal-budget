package ru.vladimir.personalaccounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.PartnerRepository;

@SpringBootTest
class DebtTransactionServiceImplTest {
	
	@Autowired
	private DebtTransactionService debtTransactionService;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private PartnerRepository pathnerRepository;
	
	@Autowired
	private UserService userService;

	@Mock
	private Authentication auth;

	private AppUser theTestUser;
//
	@BeforeEach
	public void initSecurityContext() {
//		theTestUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
//		when(auth.getPrincipal()).thenReturn(theTestUser);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
//		when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("pupa");
//		
//
	}

	
	@Test
	void testSave() {
		//save user in db
		when(SecurityContextHolder.getContext().getAuthentication().getName()).thenReturn("pupa");
		
		theTestUser = new AppUser("pupa", "pupa", "123", "123", "pupa@mail.ru", true);
		userService.save(theTestUser);
		
		//create bankaccount
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.valueOf(2000), "test bank", theTestUser,
				Currency.getInstance("RUB"));
		long bankId = bankAccountService.save(theTestBankAccount).getId();
		//create pathner
		Partner thePathner = new Partner();
		thePathner.setAppUser(theTestUser);
		thePathner.setName("test");
		
		//create debt transaction
		DebtTransaction debtTransaction = new DebtTransaction();
		debtTransaction.setAppUser(theTestUser);
		debtTransaction.setBankAccount(theTestBankAccount);
		Timestamp createTime = new Timestamp(System.currentTimeMillis());
		debtTransaction.setCreateTime(createTime);
		debtTransaction.setCurrency(theTestBankAccount.getCurrency());
		Calendar cal = Calendar.getInstance();
		cal.setTime(createTime);
		cal.add(Calendar.DAY_OF_WEEK, 30);
		Timestamp endDate = new Timestamp(cal.getTime().getTime());
		debtTransaction.setEndDate(endDate);
		debtTransaction.setPartner(thePathner);
		debtTransaction.setSumTransaction(BigDecimal.valueOf(1000));
		debtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		long id = debtTransactionService.save(debtTransaction).getId();
		//get from db
		
		when(auth.getPrincipal()).thenReturn(theTestUser);
		
		DebtTransaction debtTransactioninDb = debtTransactionService.findById(id);
		assertThat(debtTransactioninDb).isNotNull();
		
		//check bankAccount sum
		BankAccount theBankAccountInDb = bankAccountService.getBankAccountById(bankId);
		assertThat(theBankAccountInDb).isNotNull();
		assertThat(theBankAccountInDb.getBalance().compareTo(BigDecimal.valueOf(1000))).isEqualTo(0);
		
		
		

		debtTransactionService.delete(debtTransaction);
		pathnerRepository.delete(debtTransactioninDb.getPartner());
		bankAccountService.delete(theBankAccountInDb);
		
		userService.deleteUser(theTestUser);
		
		
		
		
	}	

}
