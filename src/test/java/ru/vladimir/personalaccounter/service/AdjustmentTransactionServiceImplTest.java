package ru.vladimir.personalaccounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.AdjustmentTransactionRepository;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AdjustmentTransactionServiceImplTest {
	
	@MockBean
	private AdjustmentTransactionRepository transactionRepository;
	
	@MockBean
	private BankAccountService bankAccountService;

	@InjectMocks
	private AdjustmentTransactionServiceImpl transactionService;
	
	@Test
	void testSaveShouldBeChangeBankBalance() {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
//		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		transactionService.save(usrTransaction);
		
		assertThat(theTestBankAccount.getBalance().compareTo(BigDecimal.ZERO)).isEqualTo(0);
		
	}

	@Test
	void testDelete() {
		AppUser theTestUser = new AppUser("pupa","pupa","123","123","pupa@mail.ru",true);
		BankAccount theTestBankAccount = new BankAccount("test", BigDecimal.TEN, "test bank", theTestUser, Currency.getInstance("RUB"));
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
//		usrTransaction.setAppUser(theTestUser);
		usrTransaction.setBankAccount(theTestBankAccount);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		transactionService.save(usrTransaction);
		
		assertThat(theTestBankAccount.getBalance().compareTo(BigDecimal.valueOf(20))).isEqualTo(0);
	}
	@Test
	void testSaveBankAccountIsNullShouldBeFail() {
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
//		usrTransaction.setAppUser(null);
		usrTransaction.setBankAccount(null);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		IllegalArgumentException exp = assertThrows(IllegalArgumentException.class, () -> {
			transactionService.save(usrTransaction);
		});
		
		assertThat(exp.getMessage()).isEqualTo("UsrTransaction trying save, but it has bank account is null");
		
	}
	@Test
	void testDeleteBankAccountIsNullShouldBeFail() {
		AdjustmentTransaction usrTransaction = new AdjustmentTransaction();
//		usrTransaction.setAppUser(null);
		usrTransaction.setBankAccount(null);
		usrTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
//		usrTransaction.setTypeOfTransaction(TypeOfTransaction.ADJUSTMENT);
		usrTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		usrTransaction.setSumTransaction(BigDecimal.TEN);
		IllegalArgumentException exp = assertThrows(IllegalArgumentException.class, () -> {
			transactionService.delete(usrTransaction);
		});
		
		assertThat(exp.getMessage()).isEqualTo("UsrTransaction trying delete, but it has bank account is null");
		
	}
	
	@Test
	void testDeleteCheckNull() {
		@SuppressWarnings("unused")
		NullPointerException exp = assertThrows(NullPointerException.class, () -> {
			transactionService.delete(null);
		});
	}
	@Test
	void testSafeCheckNull() {
		@SuppressWarnings("unused")
		NullPointerException exp = assertThrows(NullPointerException.class, () -> {
			transactionService.save(null);
		});
	}

}
