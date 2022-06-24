package ru.vladimir.personalaccounter.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.vladimir.personalaccounter.client.CurrencyConverterClient;
import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.repository.TransferTransactionRepository;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class TransferTransactionServiceImplTest {
	
	@MockBean
	private TransferTransactionRepository transferTransactionRepository;
	
	@MockBean
	private BankAccountService bankAccountService;
	
	@MockBean
	private CurrencyConverterClient currencyConverterClient;
	
	@InjectMocks
	private TransferTransactionServiceImpl transactionServiceImpl = new TransferTransactionServiceImpl();;
	
	@Test
	void testSaveTransferTransaction() throws CurrencyParseExcp {
		TransferTransaction theTransferTransaction = new TransferTransaction();
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setCurrency(Currency.getInstance("RUB"));
		theBankAccountOld.setBalance(BigDecimal.valueOf(1000));
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setCurrency(Currency.getInstance("USD"));
		theBankAccountNew.setBalance(BigDecimal.valueOf(1000));
		theTransferTransaction.setFromBankAccount(theBankAccountOld);
		theTransferTransaction.setToBankAccount(theBankAccountNew);
		theTransferTransaction.setSumTransactionFrom(BigDecimal.TEN);
		transactionServiceImpl.saveTransferTransaction(theTransferTransaction);
		verify(bankAccountService, times(1)).changeBalanceSaveTransaction(theTransferTransaction);
		verify(transferTransactionRepository, times(1)).save(theTransferTransaction);
	}

	

}
