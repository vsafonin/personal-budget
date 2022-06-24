package ru.vladimir.personalaccounter.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.repository.PurchaseTransactionRepository;
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class PurchaseTransactionServiceImplTest {

	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private PurchaseTransactionRepository purchaseTransactionRepository;
	
	@InjectMocks
	private PurchaseTransactionServiceImpl purchaseTransactionservice;
	
	@Test
	void testSaveShouldMinusBankAccountCreateNew() {
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(200));
		PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
		purchaseTransaction.setSumTransaction(BigDecimal.valueOf(100));
		purchaseTransaction.setBankAccount(theBankAccount);
		purchaseTransactionservice.save(purchaseTransaction);
		
		verify(bankAccountService,times(1)).changeBalanceSaveTransaction(purchaseTransaction);
	}
	@Test
	void testSaveShouldAddBankAccountEditExis() {
		
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(200));
		theBankAccount.setName("test");
		PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
		purchaseTransaction.setId(2L);
		purchaseTransaction.setSumTransaction(BigDecimal.valueOf(100));
		purchaseTransaction.setBankAccount(theBankAccount);
		PurchaseTransaction oldPurchaseTransaction = new PurchaseTransaction();
		oldPurchaseTransaction.setSumTransaction(BigDecimal.valueOf(150));
		oldPurchaseTransaction.setBankAccount(theBankAccount);
		Optional<PurchaseTransaction> oldPurchaseTransactionOptional = Optional.ofNullable(oldPurchaseTransaction);
		when(purchaseTransactionRepository.findById(2L)).thenReturn(oldPurchaseTransactionOptional);
		
		purchaseTransactionservice.save(purchaseTransaction);
		
		
		verify(bankAccountService,times(1)).changeBalanceSaveTransaction(oldPurchaseTransaction,purchaseTransaction);
	}
	@Test
	void testSaveShouldMinusBankAccountEditExis() {
		
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(200));
		theBankAccount.setName("test");
		PurchaseTransaction purchaseTransaction = new PurchaseTransaction();
		purchaseTransaction.setId(2L);
		purchaseTransaction.setSumTransaction(BigDecimal.valueOf(100));
		purchaseTransaction.setBankAccount(theBankAccount);
		PurchaseTransaction oldPurchaseTransaction = new PurchaseTransaction();
		oldPurchaseTransaction.setSumTransaction(BigDecimal.valueOf(50));
		oldPurchaseTransaction.setBankAccount(theBankAccount);
		Optional<PurchaseTransaction> oldPurchaseTransactionOptional = Optional.ofNullable(oldPurchaseTransaction);
		when(purchaseTransactionRepository.findById(2L)).thenReturn(oldPurchaseTransactionOptional);
		
		purchaseTransactionservice.save(purchaseTransaction);
		
		
		verify(bankAccountService,times(1)).changeBalanceSaveTransaction(oldPurchaseTransaction,purchaseTransaction);
	}

}
